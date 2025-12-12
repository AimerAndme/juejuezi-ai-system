package com.yupi.yuaiagent.domin.dto;

import lombok.Data;

@Data
public class CompleteUploadRequest {

    private String fileMd5;

    private String userId;
}
