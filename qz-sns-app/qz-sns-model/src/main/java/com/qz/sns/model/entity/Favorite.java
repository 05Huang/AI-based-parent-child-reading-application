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
 * 用户收藏表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("user_favorite")
@ApiModel(value = "Favorite对象", description = "用户收藏表")
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("内容ID")
    private Long contentId;

    @ApiModelProperty("收藏备注")
    private String note;

    @ApiModelProperty("收藏时间")
    private LocalDateTime createdTime;

    @ApiModelProperty("状态 1:正常 0:取消收藏")
    private Integer status;

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

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Favorite{" +
        "id = " + id +
        ", userId = " + userId +
        ", contentId = " + contentId +
        ", note = " + note +
        ", createdTime = " + createdTime +
        ", status = " + status +
        "}";
    }
}
