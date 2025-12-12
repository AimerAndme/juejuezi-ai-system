package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileUpload {

    private Long id;

    private String fileMd5;

    private String fileName;

    private Long totalSize;

    private Integer status;

    private String userId;

    private String orgTag;

    private Boolean isPublic;

    private LocalDateTime createdAt;

    private LocalDateTime mergedAt;
}
