package com.yupi.yuaiagent.domin.dto;

import lombok.Data;
import java.util.List;

@Data
public class UploadStatusResponse {

    private String fileMd5;

    private Integer status;

    private List<Integer> uploadedChunks;

    private Integer totalChunks;

    private String fileName;

    private Long totalSize;
}
