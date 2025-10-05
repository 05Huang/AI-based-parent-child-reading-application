package com.qz.sns.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户更新请求DTO
 */
@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Size(max = 64, message = "用户名长度不能超过64个字符")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;

    // 修改邮箱时的验证码
    private String verificationCode;

    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

    private String avatar;

    private String avatarThumb;

    @Min(value = 0, message = "性别值只能为0或1")
    @Max(value = 1, message = "性别值只能为0或1")
    private Integer sex;

    private Integer gender;

    private String birthday;

    private String signature;

    @Min(value = 1, message = "用户类型值范围为1-2")
    @Max(value = 2, message = "用户类型值范围为1-2")
    private Integer userType;

    @Min(value = 1, message = "角色值范围为1-2")
    @Max(value = 2, message = "角色值范围为1-2")
    private Integer role;

    private String wechatId;
}
