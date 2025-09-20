package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class RegisterByEmailDTO {
    private String email;
    private String verificationCode;
    private String password;
    private Integer role; // 角色
    private Integer terminal; // 用于区分终端类型
}