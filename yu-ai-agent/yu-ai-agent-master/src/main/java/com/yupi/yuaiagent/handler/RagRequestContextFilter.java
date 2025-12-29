package com.yupi.yuaiagent.handler;

import com.yupi.yuaiagent.domin.context.RagRequestContext;
import com.yupi.yuaiagent.domin.entity.RagRequestContextData;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RagRequestContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // 1. 从请求头/Session/JWT等解析上下文信息
           // String token = httpRequest.getHeader("Authorization");
            String userId = "1"; // 自定义逻辑
//            String tenantId = httpRequest.getHeader("X-Tenant-ID");
//            String traceId = httpRequest.getHeader("X-Trace-ID");
            RagRequestContextData ragRequestContextData = new RagRequestContextData();
            ragRequestContextData.setUserId(userId);
            // 2. 绑定到当前线程
            RagRequestContext.set(ragRequestContextData);

            // 3. 继续处理请求
            chain.doFilter(request, response);

        } finally {
            log.info("Rag问答详情内容：{}", RagRequestContext.get());
            // 4. 【关键】请求结束必须清理！
            RagRequestContext.clear();
        }
    }

    private String parseUserIdFromToken(String token) {
        // 示例：使用 JWT 解析
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            // 使用 JwtUtil 解析 claims...
            return "user123"; // 模拟
        }
        return null;
    }
}