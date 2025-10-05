package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class EmailVerifyRequest {
    private String email;
    private String ticket;  // 腾讯云验证码票据
    private String randstr; // 腾讯云验证码随机串
}