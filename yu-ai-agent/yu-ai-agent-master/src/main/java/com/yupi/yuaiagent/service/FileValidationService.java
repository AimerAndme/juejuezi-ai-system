package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.exception.FileContentException;
import com.yupi.yuaiagent.exception.FileNotExistException;
import com.yupi.yuaiagent.exception.FileValidationException;
import com.yupi.yuaiagent.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class FileValidationService {

    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = Set.of(
            ".pdf", ".txt", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"
    );

    public void validateFileInfo(Map<String, String> fileInfo) {
        if (fileInfo == null) {
            throw new FileValidationException("fileInfo", null, "文件信息不能为空");
        }

        validateFileMd5(fileInfo.get("fileMd5"));
        validateUserId(fileInfo.get("userId"));
    }

    public void validateFileUpload(FileUpload fileUpload) {
        if (fileUpload == null) {
            throw new FileValidationException("fileUpload", null, "文件上传记录不存在");
        }

        if (!fileUpload.getIsPublic()) {
            throw new FileValidationException("isPublic", fileUpload.getIsPublic(), "文件未公开，无法向量化");
        }
    }

    public void validateFilePath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new FileNotExistException(null, "文件路径为空");
        }
    }

    public void validateFileExists(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new FileNotExistException(null, "文件路径为空");
        }

        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            throw new FileNotExistException(filePath);
        }
    }

    public void validateFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new FileValidationException("fileName", fileName, "文件名不能为空");
        }

        String extension = getFileExtension(fileName);
        if (!SUPPORTED_FILE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new com.yupi.yuaiagent.exception.FileFormatException(
                    fileName,
                    extension,
                    "不支持的文件类型"
            );
        }
    }

    public void validateFileContent(String fileName, byte[] fileContent) {
        if (fileContent == null || fileContent.length == 0) {
            throw new FileContentException(fileName, "文件内容为空");
        }
    }

    private void validateFileMd5(String fileMd5) {
        if (!StringUtils.hasText(fileMd5)) {
            throw new FileValidationException("fileMd5", fileMd5, "文件MD5不能为空");
        }
    }

    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new FileValidationException("userId", userId, "用户ID不能为空");
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }
}
