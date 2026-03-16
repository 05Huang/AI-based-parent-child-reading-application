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
 * 好友
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("im_friend")
@ApiModel(value = "Friend对象", description = "好友")
public class Friend implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("好友id")
    private Long friendId;

    @ApiModelProperty("好友昵称")
    private String friendNickName;

    @ApiModelProperty("好友头像")
    private String friendHeadImage;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;
    
    @ApiModelProperty("是否置顶")
    private Boolean pinned;
    
    @ApiModelProperty("备注名")
    private String remark;
    
    @ApiModelProperty("消息清除时间")
    private LocalDateTime clearTime;

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

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getFriendNickName() {
        return friendNickName;
    }

    public void setFriendNickName(String friendNickName) {
        this.friendNickName = friendNickName;
    }

    public String getFriendHeadImage() {
        return friendHeadImage;
    }

    public void setFriendHeadImage(String friendHeadImage) {
        this.friendHeadImage = friendHeadImage;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public Boolean getPinned() {
        return pinned;
    }
    
    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public LocalDateTime getClearTime() {
        return clearTime;
    }
    
    public void setClearTime(LocalDateTime clearTime) {
        this.clearTime = clearTime;
    }

    @Override
    public String toString() {
        return "Friend{" +
        "id = " + id +
        ", userId = " + userId +
        ", friendId = " + friendId +
        ", friendNickName = " + friendNickName +
        ", friendHeadImage = " + friendHeadImage +
        ", createdTime = " + createdTime +
        ", pinned = " + pinned +
        ", remark = " + remark +
        ", clearTime = " + clearTime +
        "}";
    }
}
