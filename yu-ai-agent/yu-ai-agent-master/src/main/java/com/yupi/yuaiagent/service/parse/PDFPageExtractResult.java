package com.yupi.yuaiagent.service.parse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PDFPageExtractResult {
    private int pageIndex;
    private List<String> imageUrlList;
    private List<String> imageTextList;
    private String text;
}
