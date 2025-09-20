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
 * 内容图片关联表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("content_image")
@ApiModel(value = "ContentImage对象", description = "内容图片关联表")
public class ContentImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("内容ID")
    private Long contentId;

    @ApiModelProperty("图片URL")
    private String imageUrl;

    @ApiModelProperty("排序号")
    private Integer sortOrder;

    private LocalDateTime createdTime;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "ContentImage{" +
        "id = " + id +
        ", contentId = " + contentId +
        ", imageUrl = " + imageUrl +
        ", sortOrder = " + sortOrder +
        ", createdTime = " + createdTime +
        "}";
    }
}
