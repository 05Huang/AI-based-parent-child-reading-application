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
 * 用户浏览历史表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("user_view_history")
@ApiModel(value = "ViewHistory对象", description = "用户浏览历史表")
public class ViewHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("内容ID")
    private Long contentId;

    private LocalDateTime viewTime;

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

    public LocalDateTime getViewTime() {
        return viewTime;
    }

    public void setViewTime(LocalDateTime viewTime) {
        this.viewTime = viewTime;
    }

    @Override
    public String toString() {
        return "ViewHistory{" +
        "id = " + id +
        ", userId = " + userId +
        ", contentId = " + contentId +
        ", viewTime = " + viewTime +
        "}";
    }
}
