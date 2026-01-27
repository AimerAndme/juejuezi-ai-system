package com.yupi.yuaiagent.service.parse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfExtractResult {
    private String content;
    private boolean isSuccess;
    private int page;
}
