package com.qz.sns.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 *
 * @author Blue
 * @date 2020/10/19
 **/
@Getter//只要getter方法
@AllArgsConstructor
public enum ResultCode {
    /**
     * 成功
     */
    SUCCESS(200, "成功"),
    /**
     * 未登录
     */
    NO_LOGIN(400, "未登录"),
    /**
     * token无效或已过期
     */
    INVALID_TOKEN(401, "token无效或已过期"),
    /**
     * 系统繁忙，请稍后再试
     */
    PROGRAM_ERROR(500, "系统繁忙，请稍后再试"),
    /**
     * 密码不正确
     */
    PASSWOR_ERROR(10001, "密码不正确"),
    /**
     * 该用户名已注册
     */
    USERNAME_ALREADY_REGISTER(10003, "该用户名已注册"),
    /**
     * 请不要输入非法内容
     */
    XSS_PARAM_ERROR(10004, "请不要输入非法内容"),
    /*
    * 验证码过期或错误,最后一个枚举用分号结束
    * */
    CAPTCHA_ERROR(10005, "验证码过期或错误"),

    FORBIDDEN(403, "禁止访问，权限不足"),

    PARAM_ERROR(440,"用户ID不能为空"),

    SERVER_ERROR(441,"获取热门推荐内容失败");

    private final int code;
    private final String msg;


}

