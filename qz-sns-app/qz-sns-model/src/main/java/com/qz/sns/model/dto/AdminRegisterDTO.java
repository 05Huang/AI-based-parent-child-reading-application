package com.qz.sns.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 * 管理员注册DTO
 * </p>
 *
 * @author AI Assistant
 * @since 2025-10-23
 */
@Data
public class AdminRegisterDTO {

    /**
     * 用户名（必填）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码（必填）
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 昵称（可选，如果不填则使用用户名）
     */
    private String nickname;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 邮箱（可选）
     */
    private String email;

    /**
     * 角色（可选，管理员默认为0）
     */
    private Integer role = 0;

    /**
     * 用户类型（可选，管理员默认为2）
     */
    private Integer userType = 2;

    /**
     * 头像（可选）
     */
    private String avatar;
}







