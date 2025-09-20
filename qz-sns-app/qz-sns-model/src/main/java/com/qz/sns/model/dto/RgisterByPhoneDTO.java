package com.qz.sns.model.dto;

import lombok.Data;

/**
 * @Author: 李彬
 * @Date: 2025/3/19 下午3:55
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Data
public class RgisterByPhoneDTO {
    private String phone;
    private String verificationCode;
    private Integer terminal;
}
