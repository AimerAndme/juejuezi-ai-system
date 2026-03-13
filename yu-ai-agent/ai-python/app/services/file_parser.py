"""
文件解析服务
对应Java版本的FileParseService
"""

import os
from typing import List, Optional
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import (
    PyPDFLoader, TextLoader, Docx2txtLoader, UnstructuredExcelLoader,
)
from pypdf import PdfReader
from PIL import Image
import io

from app.core.config import settings
from app.core.logging import get_logger
from app.services.hybrid_search import HybridSearchService

logger = get_logger(__name__)


class FileParserService:
    """文件解析服务"""
    
    def __init__(self):
        self.search_service = HybridSearchService()
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=settings.parsing_chunk_size,
            chunk_overlap=settings.parsing_overlap_size,
        )
    
    async def parse_file(self, file_md5: str, user_id: str) -> int:
        """解析文件并向量化"""
        logger.info(f"开始解析文件，file_md5: {file_md5}")
        
        final_dir = settings.upload_final_dir
        
        files = os.listdir(final_dir)
        target_file = None
        for f in files:
            if f.startswith(file_md5):
                target_file = os.path.join(final_dir, f)
                break
        
        if not target_file:
            raise FileNotFoundError(f"文件不存在: {file_md5}")
        
        documents = await self._load_document(target_file)
        
        total_chunks = 0
        for doc in documents:
            chunks = self.text_splitter.split_text(doc.page_content)
            
            for i, chunk in enumerate(chunks):
                es_doc = {
                    "fileMd5": file_md5,
                    "chunkId": i,
                    "textContent": chunk,
                    "userId": user_id,
                }
                
                await self._index_document(es_doc)
                total_chunks += 1
        
        logger.info(f"文件解析完成，共 {total_chunks} 个chunks")
        return total_chunks
    
    async def _load_document(self, file_path: str) -> List:
        """加载文档"""
        ext = os.path.splitext(file_path)[1].lower()
        
        try:
            if ext == ".pdf":
                return await self._load_pdf(file_path)
            elif ext in [".txt", ".md"]:
                return await self._load_text(file_path)
            elif ext in [".doc", ".docx"]:
                return await self._load_docx(file_path)
            elif ext in [".xls", ".xlsx"]:
                return await self._load_excel(file_path)
            else:
                raise ValueError(f"不支持的文件格式: {ext}")
        except Exception as e:
            logger.error(f"加载文档失败: {str(e)}")
            raise
    
    async def _load_pdf(self, file_path: str) -> List:
        """加载PDF"""
        from langchain_community.document_loaders import PyPDFLoader
        loader = PyPDFLoader(file_path)
        return loader.load()
    
    async def _load_text(self, file_path: str) -> List:
        """加载文本文件"""
        from langchain_community.document_loaders import TextLoader
        loader = TextLoader(file_path, encoding="utf-8")
        return loader.load()
    
    async def _load_docx(self, file_path: str) -> List:
        """加载Word文档"""
        from langchain_community.document_loaders import Docx2txtLoader
        loader = Docx2txtLoader(file_path)
        return loader.load()
    
    async def _load_excel(self, file_path: str) -> List:
        """加载Excel文件"""
        from langchain_community.document_loaders import UnstructuredExcelLoader
        loader = UnstructuredExcelLoader(file_path)
        return loader.load()
    
    async def _index_document(self, doc: dict):
        """索引文档到Elasticsearch"""
        doc_id = f"{doc['fileMd5']}_{doc['chunkId']}"
        
        await self.search_service.es_client.index(
            index=HybridSearchService.INDEX_NAME,
            id=doc_id,
            document=doc,
        )
    
    async def extract_images(self, file_md5: str, user_id: str) -> List[dict]:
        """从PDF中提取图片"""
        final_dir = settings.upload_final_dir
        
        files = os.listdir(final_dir)
        target_file = None
        for f in files:
            if f.startswith(file_md5):
                target_file = os.path.join(final_dir, f)
                break
        
        if not target_file:
            return []
        
        images = []
        
        try:
            reader = PdfReader(target_file)
            for page_num, page in enumerate(reader.pages):
                for img_num, img in enumerate(page.images):
                    image_data = img.data
                    image = Image.open(io.BytesIO(image_data))
                    
                    img_path = f"upload/images/{file_md5}_p{page_num}_i{img_num}.png"
                    image.save(img_path)
                    
                    images.append({
                        "page": page_num,
                        "index": img_num,
                        "path": img_path,
                    })
        except Exception as e:
            logger.error(f"提取图片失败: {str(e)}")
        
        return images
