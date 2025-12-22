package com.yupi.yuaiagent.domin.entity;

import lombok.Data;

/**
 * 文档向量存储实体
 */
@Data
public class DocumentVector {

    /**
     * 向量记录唯一标识
     */
    private Long vectorId;

    /**
     * 关联的文件MD5值
     */
    private String fileMd5;

    /**
     * 文本分块序号
     */
    private Integer chunkId;

    /**
     * 文本内容
     */
    private String textContent;

    /**
     * 向量模型版本
     */
    private String modelVersion;

    /**
     * 上传用户ID
     */
    private String userId;

    /**
     * 文件所属组织标签
     */
    private String orgTag;

    /**
     * 文件是否公开
     */
    private Boolean isPublic;
}
