# Word文档处理测试用例文档

## 1. 测试概述

本文档描述了Word文档异步处理链路的测试用例，包括单元测试、集成测试和手动测试。

## 2. 测试文件列表

### 2.1 单元测试
- **WordContentExtractorTest.java** - WordContentExtractor单元测试
  - 位置: `src/test/java/com/yupi/yuaiagent/service/parse/WordContentExtractorTest.java`
  - 类型: Mock测试，不依赖外部资源

### 2.2 集成测试
- **FileAsyncConsumerTest.java** - FileAsyncConsumer集成测试
  - 位置: `src/test/java/com/yupi/yuaiagent/service/consumer/FileAsyncConsumerTest.java`
  - 类型: Spring Boot集成测试，使用MockBean

### 2.3 手动测试
- **WordContentExtractorManualTest.java** - 手动功能测试
  - 位置: `src/test/java/com/yupi/yuaiagent/service/parse/WordContentExtractorManualTest.java`
  - 类型: 需要实际Word文档文件的手动测试

## 3. 测试用例详情

### 3.1 WordContentExtractorTest（单元测试）

#### 3.1.1 测试用例列表

| 测试方法 | 测试场景 | 预期结果 | 优先级 |
|----------|----------|----------|--------|
| testExtractWordContentSimple | 简单模式提取Word内容 | 成功提取文本内容 | 高 |
| testExtractWordContentWithVlm_NoImages | 完整模式，无图片 | 成功提取文本，不调用VLM | 高 |
| testExtractWordContentWithVlm_WithImages | 完整模式，有图片 | 成功提取文本和图片，调用VLM | 高 |
| testExtractWordContentSimple_FileNotFound | 文件不存在 | 抛出IOException | 中 |
| testExtractWordContentWithVlm_VlmError | VLM调用失败 | 处理失败，不影响整体流程 | 中 |
| testExtractWordContentWithVlm_EmptyDocument | 空文档 | 返回空字符串 | 低 |
| testWordExtractResult | WordExtractResult构造 | 正确设置属性 | 低 |
| testWordExtractResult_Constructor | WordExtractResult构造函数 | 正确初始化对象 | 低 |

#### 3.1.2 测试方法说明

**testExtractWordContentSimple**
```java
@Test
void testExtractWordContentSimple() throws Exception
```
- **目的**: 测试简单模式下的Word内容提取
- **输入**: 包含文本的Word文档
- **预期**: 成功提取文本内容，内容不为空
- **验证点**: 
  - 返回结果不为null
  - 返回结果不为空字符串
  - 内容包含测试文本或长度大于0

**testExtractWordContentWithVlm_NoImages**
```java
@Test
void testExtractWordContentWithVlm_NoImages() throws Exception
```
- **目的**: 测试完整模式下无图片的Word文档处理
- **输入**: 不包含图片的Word文档
- **预期**: 成功提取文本，不调用VLM
- **验证点**:
  - 返回结果不为null
  - 返回结果不为空
  - vlmClient.image2Text()从未被调用

**testExtractWordContentWithVlm_WithImages**
```java
@Test
void testExtractWordContentWithVlm_WithImages() throws Exception
```
- **目的**: 测试完整模式下包含图片的Word文档处理
- **输入**: 包含图片的Word文档
- **预期**: 成功提取文本和图片，调用VLM识别
- **验证点**:
  - 返回结果不为null
  - 返回结果不为空
  - vlmClient.image2Text()被调用

**testExtractWordContentSimple_FileNotFound**
```java
@Test
void testExtractWordContentSimple_FileNotFound()
```
- **目的**: 测试文件不存在的错误处理
- **输入**: 不存在的文件路径
- **预期**: 抛出IOException
- **验证点**: 断言抛出IOException异常

**testExtractWordContentWithVlm_VlmError**
```java
@Test
void testExtractWordContentWithVlm_VlmError() throws Exception
```
- **目的**: 测试VLM调用失败的处理
- **输入**: Word文档，VLM调用抛出异常
- **预期**: 处理失败，但不影响整体流程
- **验证点**:
  - 返回结果不为null
  - 异常被正确捕获和处理

**testExtractWordContentWithVlm_EmptyDocument**
```java
@Test
void testExtractWordContentWithVlm_EmptyDocument() throws Exception
```
- **目的**: 测试空文档的处理
- **输入**: 空的Word文档
- **预期**: 返回空字符串
- **验证点**: 返回结果不为null

**testWordExtractResult**
```java
@Test
void testWordExtractResult()
```
- **目的**: 测试WordExtractResult的属性设置
- **输入**: 无
- **预期**: 正确设置text和imageUrls属性
- **验证点**:
  - text属性正确设置
  - imageUrls属性正确设置
  - imageUrls大小正确

**testWordExtractResult_Constructor**
```java
@Test
void testWordExtractResult_Constructor()
```
- **目的**: 测试WordExtractResult的构造函数
- **输入**: text和imageUrls
- **预期**: 正确初始化对象
- **验证点**:
  - text属性正确初始化
  - imageUrls属性正确初始化
  - imageUrls大小正确

### 3.2 FileAsyncConsumerTest（集成测试）

#### 3.2.1 测试用例列表

| 测试方法 | 测试场景 | 预期结果 | 优先级 |
|----------|----------|----------|--------|
| testConsumeFile_Success_PDF | 成功处理PDF文件 | 调用PDF提取器，发送ACK | 高 |
| testConsumeFile_Success_Word | 成功处理Word文件 | 调用Word提取器，发送ACK | 高 |
| testConsumeFile_DuplicateProcessing | 重复处理检测 | 发送ACK，不重复处理 | 高 |
| testConsumeFile_FileNotFound | 文件不存在 | 发送NACK，进入死信队列 | 中 |
| testConsumeFile_ValidationError | 验证失败 | 发送NACK，进入死信队列 | 中 |
| testConsumeFile_PDFExtractionSuccess | PDF提取成功 | 调用PDF提取器方法 | 高 |
| testConsumeFile_WordExtractionSuccess | Word提取成功 | 调用Word提取器方法 | 高 |
| testConsumeFile_MixedFileTypes | 混合文件类型处理 | 正确调用对应提取器 | 中 |
| testConsumeFile_BusinessProcessingFailure | 业务处理失败 | 发送NACK，进入死信队列 | 中 |

#### 3.2.2 测试方法说明

**testConsumeFile_Success_PDF**
```java
@Test
void testConsumeFile_Success_PDF() throws Exception
```
- **目的**: 测试PDF文件的成功处理流程
- **输入**: PDF文件信息
- **预期**: 调用PDF提取器，发送ACK
- **验证点**:
  - redisUtils.tryLock()被调用
  - fileValidationService.validateFileInfo()被调用
  - fileValidationService.validateFileUpload()被调用
  - pdfContentExtractor.extractMixedContentWithVlmCur()被调用

**testConsumeFile_Success_Word**
```java
@Test
void testConsumeFile_Success_Word() throws Exception
```
- **目的**: 测试Word文件的成功处理流程
- **输入**: Word文件信息
- **预期**: 调用Word提取器，发送ACK
- **验证点**:
  - redisUtils.tryLock()被调用
  - fileValidationService.validateFileInfo()被调用
  - fileValidationService.validateFileUpload()被调用
  - wordContentExtractor.extractWordContentWithVlm()被调用

**testConsumeFile_DuplicateProcessing**
```java
@Test
void testConsumeFile_DuplicateProcessing() throws Exception
```
- **目的**: 测试重复处理的检测和防护
- **输入**: 重复的文件信息
- **预期**: 发送ACK，不重复处理
- **验证点**:
  - mockChannel.basicAck()被调用
  - fileValidationService.validateFileInfo()从未被调用
  - fileUploadMapper.selectByFileMd5()从未被调用

**testConsumeFile_FileNotFound**
```java
@Test
void testConsumeFile_FileNotFound() throws Exception
```
- **目的**: 测试文件不存在的错误处理
- **输入**: 不存在的文件信息
- **预期**: 发送NACK，进入死信队列
- **验证点**:
  - mockChannel.basicNack()被调用
  - NACK参数为(false, false)，不重新入队

**testConsumeFile_ValidationError**
```java
@Test
void testConsumeFile_ValidationError() throws Exception
```
- **目的**: 测试验证失败的错误处理
- **输入**: 验证失败的文件信息
- **预期**: 发送NACK，进入死信队列
- **验证点**:
  - mockChannel.basicNack()被调用
  - NACK参数为(false, false)，不重新入队

**testConsumeFile_PDFExtractionSuccess**
```java
@Test
void testConsumeFile_PDFExtractionSuccess() throws Exception
```
- **目的**: 测试PDF提取器的调用
- **输入**: PDF文件信息
- **预期**: 正确调用PDF提取器方法
- **验证点**:
  - pdfContentExtractor.extractMixedContentWithVlmCur()被调用
  - 调用参数正确

**testConsumeFile_WordExtractionSuccess**
```java
@Test
void testConsumeFile_WordExtractionSuccess() throws Exception
```
- **目的**: 测试Word提取器的调用
- **输入**: Word文件信息
- **预期**: 正确调用Word提取器方法
- **验证点**:
  - wordContentExtractor.extractWordContentWithVlm()被调用
  - 调用参数正确

**testConsumeFile_MixedFileTypes**
```java
@Test
void testConsumeFile_MixedFileTypes() throws Exception
```
- **目的**: 测试混合文件类型的处理
- **输入**: PDF、Word、TXT等多种文件类型
- **预期**: 正确调用对应的提取器
- **验证点**:
  - PDF文件调用pdfContentExtractor
  - Word文件调用wordContentExtractor
  - 其他文件直接处理

**testConsumeFile_BusinessProcessingFailure**
```java
@Test
void testConsumeFile_BusinessProcessingFailure() throws Exception
```
- **目的**: 测试业务处理失败的错误处理
- **输入**: 处理会失败的文件信息
- **预期**: 发送NACK，进入死信队列
- **验证点**:
  - mockChannel.basicNack()被调用
  - 在超时时间内完成

### 3.3 WordContentExtractorManualTest（手动测试）

#### 3.3.1 测试用例列表

| 测试方法 | 测试场景 | 预期结果 | 优先级 |
|----------|----------|----------|--------|
| testExtractWordContentSimple_Manual | 简单模式手动测试 | 成功提取并显示内容 | 高 |
| testExtractWordContentWithVlm_Manual | 完整模式手动测试 | 成功提取并显示内容（含图片识别） | 高 |
| testExtractWordContentWithVlm_Performance | 性能测试 | 显示处理耗时 | 中 |
| testExtractMultipleWordFiles | 批量文件测试 | 显示每个文件的处理结果 | 中 |
| testExtractWordContent_ErrorHandling | 错误处理测试 | 捕获并显示异常信息 | 低 |
| testExtractWordContent_LargeFile | 大文件测试 | 显示处理结果和内存使用 | 中 |

#### 3.3.2 测试方法说明

**testExtractWordContentSimple_Manual**
```java
@Test
void testExtractWordContentSimple_Manual() throws Exception
```
- **目的**: 手动测试简单模式的Word内容提取
- **输入**: 实际的Word文档路径
- **预期**: 成功提取并显示内容
- **说明**: 需要修改wordPath为实际文件路径

**testExtractWordContentWithVlm_Manual**
```java
@Test
void testExtractWordContentWithVlm_Manual() throws Exception
```
- **目的**: 手动测试完整模式的Word内容提取
- **输入**: 实际的Word文档路径
- **预期**: 成功提取并显示内容（含图片识别）
- **说明**: 需要修改wordPath为实际文件路径

**testExtractWordContentWithVlm_Performance**
```java
@Test
void testExtractWordContentWithVlm_Performance() throws Exception
```
- **目的**: 测试Word文档处理的性能
- **输入**: 实际的Word文档路径
- **预期**: 显示处理耗时
- **说明**: 需要修改wordPath为实际文件路径

**testExtractMultipleWordFiles**
```java
@Test
void testExtractMultipleWordFiles() throws Exception
```
- **目的**: 批量测试多个Word文档的处理
- **输入**: 多个实际的Word文档路径
- **预期**: 显示每个文件的处理结果
- **说明**: 需要修改testFiles数组为实际文件路径

**testExtractWordContent_ErrorHandling**
```java
@Test
void testExtractWordContent_ErrorHandling() throws Exception
```
- **目的**: 测试错误处理机制
- **输入**: 不存在的文件路径
- **预期**: 捕获并显示异常信息
- **说明**: 验证异常处理是否正常工作

**testExtractWordContent_LargeFile**
```java
@Test
void testExtractWordContent_LargeFile() throws Exception
```
- **目的**: 测试大文件的处理性能和内存使用
- **输入**: 大型Word文档路径
- **预期**: 显示处理结果和内存使用情况
- **说明**: 需要修改wordPath为实际大文件路径

## 4. 测试执行指南

### 4.1 单元测试执行

```bash
# 执行所有单元测试
mvn test -Dtest=WordContentExtractorTest

# 执行单个测试方法
mvn test -Dtest=WordContentExtractorTest#testExtractWordContentSimple
```

### 4.2 集成测试执行

```bash
# 执行所有集成测试
mvn test -Dtest=FileAsyncConsumerTest

# 执行单个测试方法
mvn test -Dtest=FileAsyncConsumerTest#testConsumeFile_Success_Word
```

### 4.3 手动测试执行

```bash
# 执行所有手动测试
mvn test -Dtest=WordContentExtractorManualTest

# 执行单个测试方法
mvn test -Dtest=WordContentExtractorManualTest#testExtractWordContentSimple_Manual
```

### 4.4 测试前准备

1. **准备测试文件**
   - 创建测试用的Word文档（.docx, .doc）
   - 创建包含图片的Word文档
   - 创建大型Word文档用于性能测试

2. **配置测试路径**
   - 修改WordContentExtractorManualTest中的文件路径
   - 确保文件路径存在且可访问

3. **配置测试环境**
   - 确保VLM服务可用
   - 确保图片输出目录存在
   - 确保Redis服务可用（集成测试）

## 5. 测试覆盖率

### 5.1 代码覆盖率目标

| 组件 | 目标覆盖率 | 当前覆盖率 |
|------|-----------|-----------|
| WordContentExtractor | 80%+ | 待测试 |
| FileAsyncConsumer | 70%+ | 待测试 |

### 5.2 覆盖率检查

```bash
# 生成覆盖率报告
mvn clean test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html
```

## 6. 测试数据准备

### 6.1 测试文件清单

| 文件名 | 文件类型 | 内容描述 | 用途 |
|--------|----------|----------|------|
| test_simple.docx | Word | 纯文本文档 | 简单模式测试 |
| test_with_images.docx | Word | 包含图片的文档 | 完整模式测试 |
| test_no_images.docx | Word | 不包含图片的文档 | 无图片测试 |
| test_empty.docx | Word | 空文档 | 空文档测试 |
| large_test.docx | Word | 大型文档（>10MB） | 性能测试 |
| performance_test.docx | Word | 包含大量图片的文档 | 性能测试 |

### 6.2 测试文件创建

可以使用以下方式创建测试文件：

1. **手动创建**: 使用Microsoft Word创建测试文档
2. **程序生成**: 使用Apache POI生成测试文档
3. **使用模板**: 基于现有模板创建测试文档

## 7. 测试结果记录

### 7.1 测试结果模板

| 测试用例 | 执行日期 | 执行人 | 结果 | 备注 |
|----------|----------|--------|------|------|
| testExtractWordContentSimple | 2024-XX-XX | XXX | ✓/✗ | |
| testExtractWordContentWithVlm_NoImages | 2024-XX-XX | XXX | ✓/✗ | |
| testExtractWordContentWithVlm_WithImages | 2024-XX-XX | XXX | ✓/✗ | |

### 7.2 性能基准

| 测试场景 | 文件大小 | 处理耗时 | 内存使用 | 基准值 |
|----------|----------|----------|----------|--------|
| 简单Word文档 | < 1MB | < 1s | < 50MB | |
| 包含图片的Word文档 | 1-5MB | < 5s | < 100MB | |
| 大型Word文档 | > 10MB | < 30s | < 500MB | |

## 8. 常见问题

### 8.1 测试失败处理

**问题**: 测试文件不存在
```
解决方法: 修改测试代码中的文件路径为实际存在的文件路径
```

**问题**: VLM调用失败
```
解决方法: 检查VLM服务是否可用，检查API Key是否正确
```

**问题**: Redis连接失败
```
解决方法: 确保Redis服务正在运行，检查连接配置
```

### 8.2 测试环境配置

**问题**: 端口冲突
```
解决方法: 修改application-test.yml中的端口配置
```

**问题**: 权限不足
```
解决方法: 确保测试目录有读写权限
```

## 9. 持续集成

### 9.1 CI/CD集成

在CI/CD流程中添加测试步骤：

```yaml
test:
  stage: test
  script:
    - mvn clean test
    - mvn jacoco:report
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
      coverage_report:
        coverage_format: cobertura
        path: target/site/jacoco/jacoco.xml
```

### 9.2 测试报告

- 单元测试报告: target/surefire-reports/
- 覆盖率报告: target/site/jacoco/
- 集成测试报告: target/failsafe-reports/

## 10. 总结

本文档提供了完整的Word文档处理测试用例，包括：

- **8个单元测试用例**: 覆盖WordContentExtractor的主要功能
- **9个集成测试用例**: 覆盖FileAsyncConsumer的完整流程
- **6个手动测试用例**: 用于实际验证和性能测试

通过执行这些测试用例，可以确保Word文档处理功能的正确性、稳定性和性能。
