package com.qz.sns.sv.annotation;

import com.qz.sns.common.enums.UserType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限控制注解
 * 用于标识接口需要的用户权限级别
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 需要的用户类型
     * @return 用户类型数组
     */
    UserType[] value() default {};
    
    /**
     * 权限描述（可选，用于日志记录）
     * @return 权限描述
     */
    String description() default "";
}