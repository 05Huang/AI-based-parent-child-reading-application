package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Data
@ApiModel(value = "User对象", description = "用户表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("头像缩略图URL")
    private String avatarThumb;

    @ApiModelProperty("性别 0:男 1:女")
    private Integer sex;

    @ApiModelProperty("用户类型 1:普通用户 2:管理员")
    private Integer userType;

    @ApiModelProperty("角色 1:家长 2:孩子")
    private Integer role;

    @ApiModelProperty("状态 1:正常 0:禁用")
    private Integer status;

    @ApiModelProperty("微信openid")
    private String wechatId;

    @ApiModelProperty("兴趣爱好（逗号分隔的字符串）")
    private String interests;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;


}
