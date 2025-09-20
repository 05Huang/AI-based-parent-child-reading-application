package com.qz.sns.sv.config.props;

import com.qz.sns.sv.filter.CacheFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<CacheFilter> cacheFilterRegistration() {
        FilterRegistrationBean<CacheFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CacheFilter());

        // 设置要拦截的路径：所有请求
        registrationBean.addUrlPatterns("/*");

//        // 设置过滤器执行顺序，值越小越优先执行
//        registrationBean.setOrder(1);
//
//        // 可选：给 Filter 命名
//        registrationBean.setName("cacheFilter");

        return registrationBean;
    }
}
