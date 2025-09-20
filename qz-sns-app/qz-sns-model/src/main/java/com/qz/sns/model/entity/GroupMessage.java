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
 * 群消息
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("im_group_message")
@ApiModel(value = "GroupMessage对象", description = "群消息")
public class GroupMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("群id")
    private Long groupId;

    @ApiModelProperty("发送用户id")
    private Long sendId;

    @ApiModelProperty("发送用户昵称")
    private String sendNickName;

    @ApiModelProperty("接收用户id,逗号分隔，为空表示发给所有成员")
    private String recvIds;

    @ApiModelProperty("发送内容")
    private String content;

    @ApiModelProperty("被@的用户id列表，逗号分隔")
    private String atUserIds;

    @ApiModelProperty("是否回执消息")
    private Byte receipt;

    @ApiModelProperty("回执消息是否完成")
    private Byte receiptOk;

    @ApiModelProperty("消息类型 0:文字 1:图片 2:文件 3:语音 4:视频 21:提示")
    private Boolean type;

    @ApiModelProperty("状态 0:未发出 2:撤回")
    private Boolean status;

    @ApiModelProperty("发送时间")
    private LocalDateTime sendTime;

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

    public Long getSendId() {
        return sendId;
    }

    public void setSendId(Long sendId) {
        this.sendId = sendId;
    }

    public String getSendNickName() {
        return sendNickName;
    }

    public void setSendNickName(String sendNickName) {
        this.sendNickName = sendNickName;
    }

    public String getRecvIds() {
        return recvIds;
    }

    public void setRecvIds(String recvIds) {
        this.recvIds = recvIds;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAtUserIds() {
        return atUserIds;
    }

    public void setAtUserIds(String atUserIds) {
        this.atUserIds = atUserIds;
    }

    public Byte getReceipt() {
        return receipt;
    }

    public void setReceipt(Byte receipt) {
        this.receipt = receipt;
    }

    public Byte getReceiptOk() {
        return receiptOk;
    }

    public void setReceiptOk(Byte receiptOk) {
        this.receiptOk = receiptOk;
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
        return "GroupMessage{" +
        "id = " + id +
        ", groupId = " + groupId +
        ", sendId = " + sendId +
        ", sendNickName = " + sendNickName +
        ", recvIds = " + recvIds +
        ", content = " + content +
        ", atUserIds = " + atUserIds +
        ", receipt = " + receipt +
        ", receiptOk = " + receiptOk +
        ", type = " + type +
        ", status = " + status +
        ", sendTime = " + sendTime +
        "}";
    }
}
