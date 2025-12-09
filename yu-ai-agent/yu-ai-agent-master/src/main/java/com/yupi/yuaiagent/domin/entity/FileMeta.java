package com.yupi.yuaiagent.domin.entity;

import lombok.Data;
import java.io.Serializable;

// 文件元数据实体类（对应 PG jsonb 字段）
@Data
public class FileMeta implements Serializable {
    private String fileName; // 文件名
    private Long fileSize; // 文件大小（字节）
    private String fileType; // 文件类型（如 pdf、jpg）
    private String storagePath; // 存储路径
    // 可根据记忆模块需求扩展字段（如创建时间、关联记忆ID）
}