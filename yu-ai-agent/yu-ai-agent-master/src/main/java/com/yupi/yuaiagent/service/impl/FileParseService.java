package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.constant.FileUploadStatus;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.exception.BusinessException;
import com.yupi.yuaiagent.exception.ErrorCode;
import com.yupi.yuaiagent.mapper.FileUploadMapper;
import com.yupi.yuaiagent.service.IFileParseService;
import com.yupi.yuaiagent.service.IFileUploadService;
import com.yupi.yuaiagent.service.producer.MqAsyncProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileParseService implements IFileParseService {
    private final IFileUploadService fileUploadService;
    private final FileUploadMapper fileUploadMapper;
    private final MqAsyncProducer mqAsyncProducer;

    @Override
    public void parseAndVectorize(String fileMd5, String userId) {
        FileUpload fileUpload = fileUploadMapper.selectByFileMd5(fileMd5);
        if (fileUpload == null || !Objects.equals(fileUpload.getUserId(), userId) || !Objects.equals(fileUpload.getStatus(), FileUploadStatus.FILE_UPLOAD_STATUS_UPLOAD)) {
            log.error("当前文件不匹配！无法解析向量化");
            throw new BusinessException(ErrorCode.FILE_PARSE_ERROR);
        }
        mqAsyncProducer.sendFileParseFragment(fileMd5,userId);
    }
}
