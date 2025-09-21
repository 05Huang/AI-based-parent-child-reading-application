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
    private String username;
    private String phone;
    private String verificationCode;
    private String password;
    private String email;
    private String nickname;
    private String avatar;
    private Integer role = 1; // 默认为家长角色
    private Integer status = 1; // 默认为正常状态
    private Integer terminal = 1; // 默认为1
    private String interests; // 用户兴趣爱好，逗号分隔的字符串
}
