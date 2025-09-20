package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String verifyCode;
    private String newPassword;
}