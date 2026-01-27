package com.yupi.yuaiagent.service.parse;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.sax.BodyContentHandler;
@Slf4j
class StreamingContentHandler extends BodyContentHandler {

    private static final int MAX_CHUNK_SIZE = 1024 * 1024; // 1MB chunks
    private final StringBuilder content = new StringBuilder();

    public StreamingContentHandler() {
        super(-1);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        // 分块处理字符数据
        if (content.length() + length > MAX_CHUNK_SIZE) {
            // 如果添加新内容会超过阈值，先处理当前内容
            processChunk();
        }
        content.append(ch, start, length);
    }

    private void processChunk() {
        // 这里可以实现流式处理逻辑，如写入临时文件
        // 当前简化实现，保留在内存中
        log.debug("处理文本块，大小: {}", content.length());
    }

    public String getContent() {
        return content.toString();
    }
}