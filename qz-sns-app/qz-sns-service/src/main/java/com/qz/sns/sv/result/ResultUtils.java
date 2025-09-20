package com.qz.sns.sv.result;

import com.qz.sns.common.enums.ResultCode;

/**
 * 响应结果工具类，提供标准化的成功/失败响应构建方法
 * <p>本类不可实例化，通过静态方法调用使用</p>
 */
public final class ResultUtils {

    /**
     * 私有化构造方法防止实例化
     */
    private ResultUtils() {
    }

    /**
     * 创建成功响应（无数据）
     * @param <T> 响应数据类型
     * @return 标准成功响应对象，包含预定义成功状态码和消息
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMsg());
        return result;
    }

    /**
     * 创建带数据的成功响应
     * @param data 要返回的业务数据
     * @param <T> 响应数据类型
     * @return 标准成功响应对象，包含预定义成功状态码和消息，以及业务数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    /**
     * 创建自定义消息的成功响应
     * @param data 要返回的业务数据
     * @param messsage 自定义成功消息
     * @param <T> 响应数据类型
     * @return 标准成功响应对象，包含预定义成功状态码、自定义消息和业务数据
     */
    public static <T> Result<T> success(T data, String messsage) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(messsage);
        result.setData(data);
        return result;
    }

    /**
     * 创建仅包含自定义消息的成功响应
     * @param messsage 自定义成功消息
     * @param <T> 响应数据类型
     * @return 标准成功响应对象，包含预定义成功状态码和自定义消息
     */
    public static <T> Result<T> success(String messsage) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(messsage);
        return result;
    }

    /**
     * 创建自定义错误响应
     * @param code 自定义错误状态码
     * @param messsage 自定义错误描述
     * @param <T> 响应数据类型
     * @return 标准错误响应对象，包含指定错误码和错误信息
     */
    public static <T> Result<T> error(Integer code, String messsage) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(messsage);
        return result;
    }

    /**
     * 创建基于错误枚举的自定义错误响应
     * @param resultCode 错误枚举实例（提供错误状态码）
     * @param messsage 自定义错误描述
     * @param <T> 响应数据类型
     * @return 标准错误响应对象，包含枚举定义的错误码和自定义错误信息
     */
    public static <T> Result<T> error(ResultCode resultCode, String messsage) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(messsage);
        return result;
    }

    /**
     * 创建完全基于错误枚举的响应
     * @param resultCode 错误枚举实例（提供状态码和默认消息）
     * @param <T> 响应数据类型
     * @return 标准错误响应对象，包含枚举定义的错误码和默认错误信息
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMsg());
        return result;
    }
}

