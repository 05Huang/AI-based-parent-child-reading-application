package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class PhoneLoginDTO {
    private String phone;
    private String verificationCode;
    private Integer terminal = 1; // 默认为移动端
    private Integer role; // 角色：1=家长，2=孩子（可选，用于区分同一手机号的不同角色）
}
