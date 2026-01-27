package com.yupi.yuaiagent.service.parse;

import com.yupi.yuaiagent.domin.entity.FileExtractedImages;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PDF图文表混合提取结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MixedContentResult {

    private String mixedContent;  // 包含图片和表格占位符的文本内容
    private List<FileExtractedImages> fileExtractedImages;  // 图片URL列表
    private List<String> tableContents;  // 表格内容列表
    private List<Integer> errorImagePages;  // 错误图片页码列表
    private List<Integer> errorTextPages;  // 错误文本列表
}