package com.yupi.yuaiagent.domin.entity;

import lombok.Data;

@Data
public class ChunkInfo {

    private Long id;

    private String fileMd5;

    private Integer chunkIndex;

    private String chunkMd5;

    private String storagePath;
}
