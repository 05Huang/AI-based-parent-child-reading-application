package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class EmailLoginDTO {
    private String email;
    private String verificationCode;
    private Integer terminal;
}