package com.qz.sns.sv.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.qz.sns.common.enums.ResultCode;
import com.qz.sns.common.utils.JwtUtil;
import com.qz.sns.sv.config.props.JwtProperties;
import com.qz.sns.sv.exception.GlobalException;
import com.qz.sns.sv.session.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 定义白名单路径
    private final List<String> whiteList = Arrays.asList(
            "/api/user/login",           // 登录接口
            "/api/user/login-by-email",  // 邮箱登录接口
            "/api/user/login-by-phone",  // 手机号登录接口
            "/api/user/register-by-email", // 邮箱注册接口
            "/api/user/register-by-phone", // 手机号注册接口
            "/api/user/smscode",         // 获取短信验证码接口
            "/api/user/send-email-code", // 获取邮箱验证码接口
            "/api/user/check-email/**",  // 检查邮箱是否已注册接口
            "/api/user/check-phone/**",  // 检查手机号是否已注册接口
            "/api/user/reset-password",  // 重置密码接口
            "/api/file/upload",          // 文件上传接口
            "/api/category/**",          // 分类相关接口
            "/api/content/**"            // 内容相关接口（测试阶段）
    );

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("请求路径：{}", requestURI);

        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 检查是否是白名单路径
        for (String path : whiteList) {
            if (pathMatcher.match(path, requestURI)) {
                log.info("白名单路径：{}，允许访问", requestURI);
                return true;
            }
        }

        // 从 http 请求头中取出 token
        String token = request.getHeader("accessToken");
        if (StrUtil.isEmpty(token)) {
            log.error("未登录，url:{}", requestURI);
            throw new GlobalException(ResultCode.NO_LOGIN);
        }

        try {
            String strJson = JwtUtil.getInfo(token);
            UserSession userSession = JSON.parseObject(strJson, UserSession.class);
            
            // 验证 token
            if (!JwtUtil.checkSign(token, jwtProperties.getAccessTokenSecret())) {
                log.error("token已失效，用户:{}", userSession.getUserName());
                log.error("token:{}", token);
                throw new GlobalException(ResultCode.INVALID_TOKEN);
            }
            
            // 存放session
            request.setAttribute("session", userSession);
            return true;
        } catch (Exception e) {
            log.error("token验证失败：{}", e.getMessage());
            throw new GlobalException(ResultCode.INVALID_TOKEN);
        }
    }
}
