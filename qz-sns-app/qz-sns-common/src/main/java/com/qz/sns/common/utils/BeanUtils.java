package com.qz.sns.common.utils;

import org.springframework.util.ReflectionUtils;

/**
 * Bean工具类，提供属性拷贝功能（禁止实例化）
 * <p>封装Spring BeanUtils实现，统一异常处理机制</p>
 */
public final class BeanUtils {

    /**
     * 私有构造函数，防止工具类被实例化
     */
    private BeanUtils() {
    }

    /**
     * 处理反射操作异常
     *
     * @param e 反射操作过程中抛出的异常
     * @throws IllegalStateException 当发生反射相关异常时抛出
     */
    private static void handleReflectionException(Exception e) {
        ReflectionUtils.handleReflectionException(e);
    }


    /**
     * 对象属性拷贝（自动创建目标实例）
     *
     * @param orig      源对象，允许为null（返回null）
     * @param destClass 目标类类型，要求必须有无参构造函数
     * @return <T>      目标类实例，当源对象为null时返回null
     * @throws IllegalStateException 当实例创建失败或属性拷贝失败时抛出
     */
    public static <T> T copyProperties(Object orig, Class<T> destClass) {
        try {
            T target = destClass.newInstance();
            if (orig == null) {
                return null;
            }
            copyProperties(orig, target);
            return target;
        } catch (Exception e) {
            handleReflectionException(e);
            return null;
        }
    }

    /**
     * 对象属性拷贝（需提供目标实例）
     *
     * @param orig 源对象，不允许为null
     * @param dest 目标对象，不允许为null
     * @throws IllegalStateException 当属性拷贝失败时抛出
     */
    public static void copyProperties(Object orig, Object dest) {
        try {
            org.springframework.beans.BeanUtils.copyProperties(orig, dest);
        } catch (Exception e) {
            handleReflectionException(e);
        }
    }

}
