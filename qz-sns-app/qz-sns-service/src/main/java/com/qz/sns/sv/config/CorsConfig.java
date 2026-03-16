package com.qz.sns.sv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 设置允许的来源域名 - 支持所有本地开发端口
        config.addAllowedOrigin("http://localhost:5173"); // Vue/Vite 默认端口
        config.addAllowedOrigin("http://localhost:5174"); // Uni-app 应用
        config.addAllowedOrigin("http://localhost:5175"); // Uni-app H5
        config.addAllowedOrigin("http://localhost:1574");  // Uni-app 应用（备选）
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedOrigin("http://127.0.0.1:5174");
        config.addAllowedOrigin("http://127.0.0.1:5175");
        config.addAllowedOrigin("http://127.0.0.1:1574");
        // 如果需要添加多个域名
        // config.addAllowedOrigin("https://your-production-domain.com");
        
        // 允许凭证
        config.setAllowCredentials(true);
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 允许的HTTP方法
        config.addAllowedMethod("*");
        
        // 预检请求的有效期(秒)
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
