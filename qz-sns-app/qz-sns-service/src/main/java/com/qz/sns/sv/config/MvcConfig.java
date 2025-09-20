package com.qz.sns.sv.config;

import com.qz.sns.sv.interceptor.AuthInterceptor;
import com.qz.sns.sv.interceptor.XssInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final XssInterceptor xssInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(xssInterceptor).addPathPatterns("/**").excludePathPatterns("/error","/api/contents");
        registry.addInterceptor(authInterceptor).addPathPatterns("/**")
            .excludePathPatterns("/login", "/logout", "/register", "/registerByPhone","/refreshToken", "/swagger/**", "/v3/api-docs/**",
                "/swagger-resources/**", "/swagger-ui.html", "/swagger-ui/**", "/doc.html","/user/smscode","/video/upload","/api/user/register-by-email","/api/user/send-email-code","/api/user/check-email/{email}","/api/user/login-by-email");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {//作用：当用户注册时，将用户密码加密后存入数据库
        // 使用BCrypt加密密码
        return new BCryptPasswordEncoder();
    }

}
