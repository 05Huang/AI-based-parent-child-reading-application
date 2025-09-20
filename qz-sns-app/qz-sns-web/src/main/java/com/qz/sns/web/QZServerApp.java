package com.qz.sns.web;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j// 日志
@EnableAspectJAutoProxy(exposeProxy = true)//开启aop
@ComponentScan(basePackages = {"com.qz","com.qz.sns"})
@MapperScan(basePackages = {"com.qz.sns.sv.mapper"})
@SpringBootApplication
public class QZServerApp {

    public static void main(String[] args) {
        SpringApplication.run(QZServerApp.class, args);
    }

}
