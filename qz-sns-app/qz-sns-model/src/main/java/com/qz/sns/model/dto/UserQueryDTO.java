package com.qz.sns.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户查询请求DTO
 */
@Data
public class UserQueryDTO {

    private String username;

    private String phone;

    private String nickname;

    private Integer sex;

    private Integer userType;

    private Integer role;

    private Integer status;

    private String wechatId;

    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;

    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;
}