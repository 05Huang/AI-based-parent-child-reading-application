package com.qz.sns.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户登录DTO")//这个注解是swagger的注解，用于生成接口文档
public class LoginDTO {

    @Max(value = 2, message = "登录终端类型取值范围:0,2")
    @Min(value = 0, message = "登录终端类型取值范围:0,2")
    @NotNull(message = "登录终端类型不可为空")
    @Schema(description = "登录终端 0:web 1:app 2:pc")
    private Integer terminal;

    @NotEmpty(message = "用户名或邮箱不可为空")
    @Schema(description = "用户名或邮箱")
    private String userName;

    @NotEmpty(message = "用户密码不可为空")
    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "角色 1:家长 2:孩子")
    private Integer role;
}
