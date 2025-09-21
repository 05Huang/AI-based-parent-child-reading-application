package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class PhoneLoginDTO {
    private String phone;
    private String verificationCode;
    private Integer terminal = 1; // 默认为移动端
}
