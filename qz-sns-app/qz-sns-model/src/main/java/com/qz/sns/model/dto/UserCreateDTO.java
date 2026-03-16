package com.qz.sns.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户创建请求DTO
 */
@Data
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过64个字符")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

    private String avatar;

    private String avatarThumb;

    @Min(value = 0, message = "性别值只能为0或1")
    @Max(value = 1, message = "性别值只能为0或1")
    private Integer sex = 0;

    @Min(value = 1, message = "用户类型值范围为1-2")
    @Max(value = 2, message = "用户类型值范围为1-2")
    private Integer userType = 1;

    @Min(value = 0, message = "角色值范围为0-2")
    @Max(value = 2, message = "角色值范围为0-2")
    private Integer role = 1;

    private String wechatId;
}