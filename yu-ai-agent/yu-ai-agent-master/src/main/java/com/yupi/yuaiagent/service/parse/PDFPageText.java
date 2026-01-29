package com.yupi.yuaiagent.service.parse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PDFPageText {
    private int pageIndex;
    private String text;
}
