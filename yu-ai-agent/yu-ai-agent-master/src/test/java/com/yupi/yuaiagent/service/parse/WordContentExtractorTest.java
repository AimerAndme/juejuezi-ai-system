//package com.yupi.yuaiagent.service.parse;
//
//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.alibaba.dashscope.exception.UploadFileException;
//import com.yupi.yuaiagent.client.VlmClient;
//import com.yupi.yuaiagent.domin.entity.FileUpload;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//class WordContentExtractorTest {
//
//    @Mock
//    private VlmClient vlmClient;
//
//    @Mock
//    private ThreadPoolTaskExecutor pdfPageExecutor;
//
//    @InjectMocks
//    private WordContentExtractor wordContentExtractor;
//
//    @TempDir
//    Path tempDir;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testExtractWordContentSimple() throws Exception {
//        String testContent = "这是一个测试文档内容。\n\n这是第二段内容。";
//        File wordFile = createTestWordFile("test_simple.docx", testContent);
//
//        String result = wordContentExtractor.extractWordContentSimple(wordFile.getAbsolutePath());
//
//        assertNotNull(result);
//        assertFalse(result.isEmpty());
//        assertTrue(result.contains("测试") || result.length() > 0);
//    }
//
//    @Test
//    void testExtractWordContentWithVlm_NoImages() throws Exception {
//        FileUpload fileUpload = createMockFileUpload("test_no_images.docx", "test123");
//        String testContent = "这是一个没有图片的文档。";
//        File wordFile = createTestWordFile("test_no_images.docx", testContent);
//        File imageDir = tempDir.resolve("images").toFile();
//        imageDir.mkdirs();
//
//        String result = wordContentExtractor.extractWordContentWithVlm(
//            fileUpload,
//            wordFile.getAbsolutePath(),
//            imageDir.getAbsolutePath(),
//            "http://localhost/images/"
//        );
//
//        assertNotNull(result);
//        assertFalse(result.isEmpty());
//        verify(vlmClient, never()).image2Text(anyString());
//    }
//
//    @Test
//    void testExtractWordContentWithVlm_WithImages() throws Exception {
//        FileUpload fileUpload = createMockFileUpload("test_with_images.docx", "test456");
//        String testContent = "这是一个包含图片的文档。";
//        File wordFile = createTestWordFile("test_with_images.docx", testContent);
//        File imageDir = tempDir.resolve("images").toFile();
//        imageDir.mkdirs();
//
//        when(vlmClient.image2Text(anyString())).thenReturn("图片识别结果：这是一张测试图片");
//
//        String result = wordContentExtractor.extractWordContentWithVlm(
//            fileUpload,
//            wordFile.getAbsolutePath(),
//            imageDir.getAbsolutePath(),
//            "http://localhost/images/"
//        );
//
//        assertNotNull(result);
//        assertFalse(result.isEmpty());
//    }
//
//    @Test
//    void testExtractWordContentSimple_FileNotFound() {
//        String nonExistentPath = tempDir.resolve("non_existent.docx").toString();
//
//        assertThrows(IOException.class, () -> {
//            wordContentExtractor.extractWordContentSimple(nonExistentPath);
//        });
//    }
//
//    @Test
//    void testExtractWordContentWithVlm_VlmError() throws Exception {
//        FileUpload fileUpload = createMockFileUpload("test_vlm_error.docx", "test789");
//        String testContent = "这是一个测试文档。";
//        File wordFile = createTestWordFile("test_vlm_error.docx", testContent);
//        File imageDir = tempDir.resolve("images").toFile();
//        imageDir.mkdirs();
//
//        when(vlmClient.image2Text(anyString()))
//            .thenThrow(new NoApiKeyException("API Key错误"));
//
//        String result = wordContentExtractor.extractWordContentWithVlm(
//            fileUpload,
//            wordFile.getAbsolutePath(),
//            imageDir.getAbsolutePath(),
//            "http://localhost/images/"
//        );
//
//        assertNotNull(result);
//    }
//
//    @Test
//    void testExtractWordContentWithVlm_EmptyDocument() throws Exception {
//        FileUpload fileUpload = createMockFileUpload("test_empty.docx", "test_empty");
//        String testContent = "";
//        File wordFile = createTestWordFile("test_empty.docx", testContent);
//        File imageDir = tempDir.resolve("images").toFile();
//        imageDir.mkdirs();
//
//        String result = wordContentExtractor.extractWordContentWithVlm(
//            fileUpload,
//            wordFile.getAbsolutePath(),
//            imageDir.getAbsolutePath(),
//            "http://localhost/images/"
//        );
//
//        assertNotNull(result);
//    }
//
//    @Test
//    void testWordExtractResult() {
//        WordContentExtractor.WordExtractResult result = new WordContentExtractor.WordExtractResult();
//        result.setText("测试文本");
//        result.setImageUrls(List.of("http://localhost/images/image1.png", "http://localhost/images/image2.png"));
//
//        assertEquals("测试文本", result.getText());
//        assertEquals(2, result.getImageUrls().size());
//        assertEquals("http://localhost/images/image1.png", result.getImageUrls().get(0));
//    }
//
//    @Test
//    void testWordExtractResult_Constructor() {
//        WordContentExtractor.WordExtractResult result = new WordContentExtractor.WordExtractResult(
//            "测试文本",
//            List.of("http://localhost/images/image1.png")
//        );
//
//        assertEquals("测试文本", result.getText());
//        assertEquals(1, result.getImageUrls().size());
//    }
//
//    private FileUpload createMockFileUpload(String fileName, String fileMd5) {
//        FileUpload fileUpload = new FileUpload();
//        fileUpload.setFileName(fileName);
//        fileUpload.setFileMd5(fileMd5);
//        fileUpload.setUserId("test_user");
//        return fileUpload;
//    }
//
//    private File createTestWordFile(String fileName, String content) throws IOException {
//        File file = tempDir.resolve(fileName).toFile();
//
//        try (FileWriter writer = new FileWriter(file)) {
//            writer.write(content);
//        }
//
//        return file;
//    }
//}
