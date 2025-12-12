package com.yupi.yuaiagent.domin.dto;

import lombok.Data;

@Data
public class InitiateUploadRequest {

    private String fileName;

    private Long totalSize;

    private String fileMd5;

    private String userId;

    private Boolean isPublic;
}
