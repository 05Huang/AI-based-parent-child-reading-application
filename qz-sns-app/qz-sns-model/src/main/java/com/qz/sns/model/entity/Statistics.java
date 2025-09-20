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
 * 用户统计数据表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("user_statistics")
@ApiModel(value = "Statistics对象", description = "用户统计数据表")
public class Statistics implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("注册天数")
    private Integer registerDays;

    @ApiModelProperty("最后在线时间")
    private LocalDateTime lastOnlineTime;

    @ApiModelProperty("累计登录天数")
    private Integer loginDays;

    @ApiModelProperty("与父亲聊天消息数")
    private Integer fatherChatCount;

    @ApiModelProperty("与母亲聊天消息数")
    private Integer motherChatCount;

    @ApiModelProperty("最后与父亲聊天时间")
    private LocalDateTime lastFatherChatTime;

    @ApiModelProperty("最后与母亲聊天时间")
    private LocalDateTime lastMotherChatTime;

    @ApiModelProperty("主动与父亲聊天次数")
    private Integer fatherChatInitiative;

    @ApiModelProperty("主动与母亲聊天次数")
    private Integer motherChatInitiative;

    @ApiModelProperty("群聊消息数")
    private Integer groupMessageCount;

    @ApiModelProperty("群聊活跃天数")
    private Integer groupActiveDays;

    @ApiModelProperty("AI对话次数")
    private Integer aiChatCount;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRegisterDays() {
        return registerDays;
    }

    public void setRegisterDays(Integer registerDays) {
        this.registerDays = registerDays;
    }

    public LocalDateTime getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(LocalDateTime lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public Integer getLoginDays() {
        return loginDays;
    }

    public void setLoginDays(Integer loginDays) {
        this.loginDays = loginDays;
    }

    public Integer getFatherChatCount() {
        return fatherChatCount;
    }

    public void setFatherChatCount(Integer fatherChatCount) {
        this.fatherChatCount = fatherChatCount;
    }

    public Integer getMotherChatCount() {
        return motherChatCount;
    }

    public void setMotherChatCount(Integer motherChatCount) {
        this.motherChatCount = motherChatCount;
    }

    public LocalDateTime getLastFatherChatTime() {
        return lastFatherChatTime;
    }

    public void setLastFatherChatTime(LocalDateTime lastFatherChatTime) {
        this.lastFatherChatTime = lastFatherChatTime;
    }

    public LocalDateTime getLastMotherChatTime() {
        return lastMotherChatTime;
    }

    public void setLastMotherChatTime(LocalDateTime lastMotherChatTime) {
        this.lastMotherChatTime = lastMotherChatTime;
    }

    public Integer getFatherChatInitiative() {
        return fatherChatInitiative;
    }

    public void setFatherChatInitiative(Integer fatherChatInitiative) {
        this.fatherChatInitiative = fatherChatInitiative;
    }

    public Integer getMotherChatInitiative() {
        return motherChatInitiative;
    }

    public void setMotherChatInitiative(Integer motherChatInitiative) {
        this.motherChatInitiative = motherChatInitiative;
    }

    public Integer getGroupMessageCount() {
        return groupMessageCount;
    }

    public void setGroupMessageCount(Integer groupMessageCount) {
        this.groupMessageCount = groupMessageCount;
    }

    public Integer getGroupActiveDays() {
        return groupActiveDays;
    }

    public void setGroupActiveDays(Integer groupActiveDays) {
        this.groupActiveDays = groupActiveDays;
    }

    public Integer getAiChatCount() {
        return aiChatCount;
    }

    public void setAiChatCount(Integer aiChatCount) {
        this.aiChatCount = aiChatCount;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "Statistics{" +
        "id = " + id +
        ", userId = " + userId +
        ", registerDays = " + registerDays +
        ", lastOnlineTime = " + lastOnlineTime +
        ", loginDays = " + loginDays +
        ", fatherChatCount = " + fatherChatCount +
        ", motherChatCount = " + motherChatCount +
        ", lastFatherChatTime = " + lastFatherChatTime +
        ", lastMotherChatTime = " + lastMotherChatTime +
        ", fatherChatInitiative = " + fatherChatInitiative +
        ", motherChatInitiative = " + motherChatInitiative +
        ", groupMessageCount = " + groupMessageCount +
        ", groupActiveDays = " + groupActiveDays +
        ", aiChatCount = " + aiChatCount +
        ", createdTime = " + createdTime +
        ", updatedTime = " + updatedTime +
        "}";
    }
}
