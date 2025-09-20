package com.qz.sns.sv.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class CacheFilter extends OncePerRequestFilter {

    /**
     * 处理HTTP请求的过滤器方法，用于包装可缓存的请求对象
     *
     * @param request     HTTP请求对象，将被包装为可缓存请求
     * @param response    HTTP响应对象，保持不变传递
     * @param filterChain 过滤器链，用于继续执行后续过滤器或目标资源
     * @throws ServletException 如果处理过程中发生servlet相关的异常
     * @throws IOException      如果发生输入输出相关的异常
     *
     * 实现逻辑：
     * 1. 检查当前请求是否已被包装为缓存请求对象
     * 2. 若未包装则创建新的缓存请求包装器
     * 3. 将（可能被包装后的）请求继续传递到过滤器链
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        // 包装原始请求对象以支持多次读取请求体
        if (!(request instanceof CacheHttpServletRequestWrapper)) {
            request = new CacheHttpServletRequestWrapper(request);
        }

        // 将处理后的请求传递给后续过滤器或目标资源
        filterChain.doFilter(request, response);
    }
}
