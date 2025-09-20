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
 * 群
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("im_group")
@ApiModel(value = "Group对象", description = "群")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("群名字")
    private String name;

    @ApiModelProperty("群主id")
    private Long ownerId;

    @ApiModelProperty("群头像")
    private String headImage;

    @ApiModelProperty("群头像缩略图")
    private String headImageThumb;

    @ApiModelProperty("群公告")
    private String notice;

    @ApiModelProperty("是否被封禁 0:否 1:是")
    private Boolean isBanned;

    @ApiModelProperty("被封禁原因")
    private String reason;

    @ApiModelProperty("是否已解散")
    private Boolean dissolve;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public String getHeadImageThumb() {
        return headImageThumb;
    }

    public void setHeadImageThumb(String headImageThumb) {
        this.headImageThumb = headImageThumb;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public Boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(Boolean isBanned) {
        this.isBanned = isBanned;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getDissolve() {
        return dissolve;
    }

    public void setDissolve(Boolean dissolve) {
        this.dissolve = dissolve;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "Group{" +
        "id = " + id +
        ", name = " + name +
        ", ownerId = " + ownerId +
        ", headImage = " + headImage +
        ", headImageThumb = " + headImageThumb +
        ", notice = " + notice +
        ", isBanned = " + isBanned +
        ", reason = " + reason +
        ", dissolve = " + dissolve +
        ", createdTime = " + createdTime +
        "}";
    }
}
