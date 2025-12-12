package com.yupi.yuaiagent.domin.dto;

import lombok.Data;

@Data
public class UploadChunkRequest {

    private String fileMd5;

    private Integer chunkIndex;

    private String chunkMd5;

    private String userId;
}
