package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 群成员
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("im_group_member")
@ApiModel(value = "GroupMember对象", description = "群成员")
public class GroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("群id")
    private Long groupId;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户昵称")
    private String userNickName;

    @ApiModelProperty("显示昵称备注")
    private String remarkNickName;

    @ApiModelProperty("用户头像")
    private String headImage;

    @ApiModelProperty("显示群名备注")
    private String remarkGroupName;

    @ApiModelProperty("是否已退出")
    private Boolean quit;

    @ApiModelProperty("退出时间")
    private LocalDateTime quitTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getRemarkNickName() {
        return remarkNickName;
    }

    public void setRemarkNickName(String remarkNickName) {
        this.remarkNickName = remarkNickName;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public String getRemarkGroupName() {
        return remarkGroupName;
    }

    public void setRemarkGroupName(String remarkGroupName) {
        this.remarkGroupName = remarkGroupName;
    }

    public Boolean getQuit() {
        return quit;
    }

    public void setQuit(Boolean quit) {
        this.quit = quit;
    }

    public LocalDateTime getQuitTime() {
        return quitTime;
    }

    public void setQuitTime(LocalDateTime quitTime) {
        this.quitTime = quitTime;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "GroupMember{" +
        "id = " + id +
        ", groupId = " + groupId +
        ", userId = " + userId +
        ", userNickName = " + userNickName +
        ", remarkNickName = " + remarkNickName +
        ", headImage = " + headImage +
        ", remarkGroupName = " + remarkGroupName +
        ", quit = " + quit +
        ", quitTime = " + quitTime +
        ", createdTime = " + createdTime +
        "}";
    }
}
