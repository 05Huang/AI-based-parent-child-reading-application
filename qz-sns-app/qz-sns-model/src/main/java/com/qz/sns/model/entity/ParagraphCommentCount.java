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
 * 段落评论计数表
 * </p>
 *
 * @author 李彬
 * @since 2025-04-17
 */
@TableName("paragraph_comment_count")
@ApiModel(value = "ParagraphCommentCount对象", description = "段落评论计数表")
public class ParagraphCommentCount implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("内容 ID")
    private Long contentId;

    @ApiModelProperty("段落标识符")
    private String paragraphId;

    @ApiModelProperty("评论数")
    private Integer commentCount;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    public ParagraphCommentCount(Long contentId, String paragraphId) {
        this.contentId = contentId;
        this.paragraphId = paragraphId;
    }

    public ParagraphCommentCount() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(String paragraphId) {
        this.paragraphId = paragraphId;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
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
        return "ParagraphCommentCount{" +
        "id = " + id +
        ", contentId = " + contentId +
        ", paragraphId = " + paragraphId +
        ", commentCount = " + commentCount +
        ", createdTime = " + createdTime +
        ", updatedTime = " + updatedTime +
        "}";
    }
}
