package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class EmailLoginDTO {
    private String email;
    private String verificationCode;
    private Integer terminal;
    private Integer role; // 角色：1=家长，2=孩子
}