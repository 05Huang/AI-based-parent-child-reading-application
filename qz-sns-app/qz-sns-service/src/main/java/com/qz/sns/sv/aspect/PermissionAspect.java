package com.qz.sns.sv.aspect;


import com.qz.sns.common.enums.ResultCode;
import com.qz.sns.common.enums.UserType;
import com.qz.sns.sv.annotation.RequirePermission;
import com.qz.sns.sv.exception.GlobalException;
import com.qz.sns.sv.session.SessionContext;
import com.qz.sns.sv.session.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 权限检查切面
 * 用于拦截带有@RequirePermission注解的方法，进行权限验证
 */
@Slf4j
@Aspect
@Component
@Order(1) // 设置切面执行顺序，数字越小优先级越高
public class PermissionAspect {

    /**
     * 在方法执行前进行权限检查
     * 
     * @param joinPoint 切入点
     * @param requirePermission 权限注解
     */
    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        // 获取当前用户类型
        Integer currentUserType = getCurrentUserType();
        
        // 如果无法获取用户类型，说明用户未登录
        if (currentUserType == null) {
            log.warn("用户未登录，拒绝访问接口：{}", getMethodName(joinPoint));
            throw new GlobalException(ResultCode.NO_LOGIN, "请先登录");
        }
        
        // 获取注解中要求的用户类型
        UserType[] requiredTypes = requirePermission.value();
        
        // 如果注解中没有指定用户类型，则允许所有已登录用户访问
        if (requiredTypes.length == 0) {
            return;
        }
        
        // 检查当前用户类型是否在允许的类型列表中
        UserType currentType = UserType.getByCode(currentUserType);
        boolean hasPermission = Arrays.stream(requiredTypes)
                .anyMatch(type -> type == currentType);
        
        if (!hasPermission) {
            String methodName = getMethodName(joinPoint);
            String requiredTypesStr = Arrays.toString(requiredTypes);
            String currentTypeStr = currentType != null ? currentType.getDescription() : "未知";
            
            log.warn("用户权限不足，当前用户类型：{}，需要权限：{}，访问接口：{}", 
                    currentTypeStr, requiredTypesStr, methodName);
            
            throw new GlobalException(ResultCode.FORBIDDEN,
                    String.format("权限不足，需要%s权限", requiredTypesStr));
        }
        
        log.info("权限检查通过，用户类型：{}，访问接口：{}", 
                currentType != null ? currentType.getDescription() : "未知", 
                getMethodName(joinPoint));
    }
    
    /**
     * 获取当前用户类型
     * 这里需要根据您的实际实现来获取用户类型
     * 
     * @return 当前用户类型
     */
    private Integer getCurrentUserType() {
        try {
            UserSession sessionContext = SessionContext.getSession();
            if (sessionContext == null || sessionContext.getUserType() == null) {
                return null; // 用户未登录
            }
            // 假设UserSession中有getUserType方法返回用户类型
            return sessionContext.getUserType();
            
        } catch (Exception e) {
            log.error("获取当前用户类型失败：{}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取方法全名（类名.方法名）
     * 
     * @param joinPoint 切入点
     * @return 方法全名
     */
    private String getMethodName(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
}