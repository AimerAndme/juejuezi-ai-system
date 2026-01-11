package com.yupi.yuaiagent.domin.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * 文件提取图片实体类 对应数据库表 file_extracted_images
 */
@Data
@Accessors(chain = true)
public class FileExtractedImages {

    /**
     * 自增ID，作为主键
     */
    private Integer id;

    /**
     * 关联到上传或操作该文件的用户ID
     */
    private String userId;

    /**
     * 关联到源文件的MD5值
     */
    private String fileMd5;

    /**
     * 图片在服务器上的存储路径
     */
    private String imagePath;

    /**
     * 图片在原文件中的页码
     */
    private Integer pageNumber;

    /**
     * 图片在原PDF页面上的坐标信息 (例如: {"x0": 100, "y0": 200, "x1": 300, "y1": 400})
     */
    private JsonNode extractionBbox;

    /**
     * 执行VLM识别的模型名称
     */
    private String vlModelName;

    /**
     * 执行VLM识别的模型版本
     */
    private String vlModelVersion;

    /**
     * VLM模型生成的图片描述、摘要或识别出的文本内容
     */
    private String vlResultText;

    /**
     * VLM模型更结构化的输出结果，如对象检测框、分类标签及其置信度等
     */
    private JsonNode vlResultJson;

    /**
     * VLM处理状态: 'pending', 'processing', 'completed', 'failed'
     */
    private String vlProcessingStatus;

    /**
     * VLM处理完成的时间戳
     */
    private Timestamp vlProcessedAt;

    /**
     * 记录创建时间
     */
    private Timestamp createdAt;

    /**
     * 记录更新时间
     */
    private Timestamp updatedAt;
}
