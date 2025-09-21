package com.qz.sns.sv.config;

import com.qz.sns.sv.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器，并排除不需要拦截的路径
        registry.addInterceptor(authInterceptor)
                .excludePathPatterns(
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
                        "/api/file/upload"           // 文件上传接口
                );
    }
}
