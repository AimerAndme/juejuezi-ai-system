-- 创建表
CREATE TABLE file_extracted_images (
                                      id SERIAL PRIMARY KEY, -- 自增ID，作为主键
                                      user_id INT NOT NULL, -- 关联到上传或操作该文件的用户ID
                                      file_md5 VARCHAR(32) NOT NULL, -- 关联到源文件的MD5值
                                      image_path TEXT, -- 图片在服务器上的存储路径
    -- image_data BYTEA,      -- (可选) 如果选择直接存储图片二进制数据，而不是路径。通常不推荐，除非图片很小且数量不多。
                                      page_number INT, -- 图片在原文件中的页码 (可选，但通常很有用)
                                      extraction_bbox JSONB, -- 图片在原PDF页面上的坐标信息 (例如: {"x0": 100, "y0": 200, "x1": 300, "y1": 400})，JSONB方便查询
                                      vl_model_name VARCHAR(255), -- 执行VLM识别的模型名称 (可选，用于区分不同模型的结果)
                                      vl_model_version VARCHAR(50), -- 执行VLM识别的模型版本 (可选)
                                      vl_result_text TEXT, -- VLM模型生成的图片描述、摘要或识别出的文本内容
                                      vl_result_json JSONB, -- (可选) VLM模型更结构化的输出结果，如对象检测框、分类标签及其置信度等，存储为JSONB
                                      vl_processing_status VARCHAR(20) DEFAULT 'pending', -- VLM处理状态: 'pending', 'processing', 'completed', 'failed'
                                      vl_error_message TEXT, -- 如果VLM处理失败，记录错误信息
                                      vl_processed_at TIMESTAMP WITH TIME ZONE, -- VLM处理完成的时间戳
                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- 记录创建时间
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP -- 记录更新时间

    -- 添加外键约束 (假设存在名为 users 和 pdf_files 的表)
    -- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE -- 假设用户表名为 users，主键为 id
    -- FOREIGN KEY (file_md5) REFERENCES pdf_files(md5) ON DELETE CASCADE -- 假设PDF文件表名为 pdf_files，主键为 md5
    -- 注意：如果pdf_files表的md5列不是主键，需要先在该列上创建唯一索引
    -- CONSTRAINT fk_pdf_file FOREIGN KEY (file_md5) REFERENCES pdf_files(md5)
    -- CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 添加表注释
COMMENT ON TABLE file_extracted_images IS '用于管理从文档中提取的图片及其VLM模型识别结果的表';

-- 添加列注释
COMMENT ON COLUMN file_extracted_images.id IS '主键，自增ID';
COMMENT ON COLUMN file_extracted_images.user_id IS '关联到上传或操作该文件的用户ID，用于权限控制';
COMMENT ON COLUMN file_extracted_images.file_md5 IS '关联到源文件的MD5哈希值，用于标识所属文档';
COMMENT ON COLUMN file_extracted_images.image_path IS '提取出的图片文件在服务器上的存储路径';
COMMENT ON COLUMN file_extracted_images.page_number IS '图片在原始文档中的页码';
COMMENT ON COLUMN file_extracted_images.extraction_bbox IS '图片在原页面上的坐标边界框，格式为JSON，如 {"x0": 100, "y0": 200, "x1": 300, "y1": 400}';
COMMENT ON COLUMN file_extracted_images.vl_model_name IS '执行视觉语言模型（VLM）识别所使用的模型名称';
COMMENT ON COLUMN file_extracted_images.vl_model_version IS '执行VLM识别所使用的模型版本号';
COMMENT ON COLUMN file_extracted_images.vl_result_text IS 'VLM模型生成的图片文本描述或摘要';
COMMENT ON COLUMN file_extracted_images.vl_result_json IS 'VLM模型生成的结构化结果，如检测框、标签、置信度等，存储为JSONB格式';
COMMENT ON COLUMN file_extracted_images.vl_processing_status IS 'VLM处理状态，可能值: pending, processing, completed, failed';
COMMENT ON COLUMN file_extracted_images.vl_error_message IS 'VLM处理失败时记录的错误信息';
COMMENT ON COLUMN file_extracted_images.vl_processed_at IS 'VLM模型处理完成的时间戳';
COMMENT ON COLUMN file_extracted_images.created_at IS '该记录在数据库中创建的时间戳';
COMMENT ON COLUMN file_extracted_images.updated_at IS '该记录最后一次被更新的时间戳';

-- 为常用查询字段创建索引
CREATE INDEX idx_file_extracted_images_file_md5 ON file_extracted_images(file_md5); -- 加速通过文件MD5查询
CREATE INDEX idx_file_extracted_images_user_id ON file_extracted_images(user_id); -- 加速通过用户ID查询
--CREATE INDEX idx_file_extracted_images_page_number ON file_extracted_images(page_number); -- 加速通过页码查询
--CREATE INDEX idx_file_extracted_images_vl_processing_status ON file_extracted_images(vl_processing_status); -- 加速查询待处理或失败的记录
--CREATE INDEX idx_file_extracted_images_vl_processed_at ON file_extracted_images(vl_processed_at); -- 加速按处理时间查询
-- 如果 vl_result_text 用于全文检索，可以创建GIN索引
-- CREATE INDEX idx_file_extracted_images_vl_result_text_gin ON file_extracted_images USING GIN (to_tsvector('english', vl_result_text));
-- 如果 vl_result_json 用于查询，可以创建GIN索引
--CREATE INDEX idx_file_extracted_images_vl_result_json_gin ON file_extracted_images USING GIN (vl_result_json);

-- 可选：如果需要对 image_path 进行模糊查询，可以创建索引
-- CREATE INDEX idx_file_extracted_images_image_path ON file_extracted_images(image_path);