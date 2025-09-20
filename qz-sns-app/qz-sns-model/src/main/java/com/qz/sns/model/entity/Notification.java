package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 通知表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@ApiModel(value = "Notification对象", description = "通知表")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("接收用户ID")
    private Long userId;

    @ApiModelProperty("类型 1:点赞 2:评论 3:回复 4:系统")
    private Boolean type;

    @ApiModelProperty("通知内容")
    private String content;

    @ApiModelProperty("目标ID")
    private Long targetId;

    @ApiModelProperty("来源用户ID")
    private Long fromUserId;

    @ApiModelProperty("是否已读 0:未读 1:已读")
    private Boolean isRead;

    private LocalDateTime createdTime;

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

    public Boolean getType() {
        return type;
    }

    public void setType(Boolean type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "Notification{" +
        "id = " + id +
        ", userId = " + userId +
        ", type = " + type +
        ", content = " + content +
        ", targetId = " + targetId +
        ", fromUserId = " + fromUserId +
        ", isRead = " + isRead +
        ", createdTime = " + createdTime +
        "}";
    }
}
