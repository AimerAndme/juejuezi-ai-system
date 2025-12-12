package com.yupi.yuaiagent.domin.dto;

import lombok.Data;
import java.util.List;

@Data
public class InitiateUploadResponse {

    private Boolean needUpload;

    private String fileMd5;

    private List<Integer> uploadedChunks;

    private Integer totalChunks;

    private String message;
}
