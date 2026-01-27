package com.yupi.yuaiagent.service.parse;

import com.yupi.yuaiagent.domin.entity.FileExtractedImages;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
class PageProcessResult {
    private int pageIndex;
    private String pageContent;
    private boolean isImageParsed;
    private boolean isTextParsed;
    private List<FileExtractedImages> imageUrls;
    private List<String> tableContents;
}