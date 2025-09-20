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
 * 私聊消息
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("im_private_message")
@ApiModel(value = "PrivateMessage对象", description = "私聊消息")
public class PrivateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("发送用户id")
    private Long sendId;

    @ApiModelProperty("接收用户id")
    private Long recvId;

    @ApiModelProperty("发送内容")
    private String content;

    @ApiModelProperty("消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示")
    private Boolean type;

    @ApiModelProperty("状态 0:未读 1:已读 2:撤回 3:已读")
    private Boolean status;

    @ApiModelProperty("发送时间")
    private LocalDateTime sendTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSendId() {
        return sendId;
    }

    public void setSendId(Long sendId) {
        this.sendId = sendId;
    }

    public Long getRecvId() {
        return recvId;
    }

    public void setRecvId(Long recvId) {
        this.recvId = recvId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getType() {
        return type;
    }

    public void setType(Boolean type) {
        this.type = type;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        return "PrivateMessage{" +
        "id = " + id +
        ", sendId = " + sendId +
        ", recvId = " + recvId +
        ", content = " + content +
        ", type = " + type +
        ", status = " + status +
        ", sendTime = " + sendTime +
        "}";
    }
}
