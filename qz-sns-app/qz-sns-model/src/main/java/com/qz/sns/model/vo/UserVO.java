package com.qz.sns.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户VO (不包含敏感信息)
 */
@Data
@ApiModel(value = "UserVO对象", description = "用户视图对象（隐藏敏感信息）")
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("头像缩略图URL")
    private String avatarThumb;

    @ApiModelProperty("性别 0:男 1:女")
    private Boolean sex;

    @ApiModelProperty("用户类型 1:普通用户 2:管理员")
    private Boolean userType;

    @ApiModelProperty("角色 1:家长 2:孩子")
    private Boolean role;

    @ApiModelProperty("状态 1:正常 0:禁用")
    private Boolean status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedTime;


}
