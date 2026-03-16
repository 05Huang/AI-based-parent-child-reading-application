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
 * AI助手对话记录表
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@TableName("ai_chat_record")
@ApiModel(value = "AiChatRecord对象", description = "AI助手对话记录表")
public class AiChatRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("群组ID")
    private Long groupId;

    @ApiModelProperty("消息ID")
    private Long messageId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户问题")
    private String userQuestion;

    @ApiModelProperty("AI回复")
    private String aiReply;

    @ApiModelProperty("使用的token数")
    private Integer tokensUsed;

    private LocalDateTime createdTime;

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

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserQuestion() {
        return userQuestion;
    }

    public void setUserQuestion(String userQuestion) {
        this.userQuestion = userQuestion;
    }

    public String getAiReply() {
        return aiReply;
    }

    public void setAiReply(String aiReply) {
        this.aiReply = aiReply;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "AiChatRecord{" +
        "id = " + id +
        ", groupId = " + groupId +
        ", messageId = " + messageId +
        ", userId = " + userId +
        ", userQuestion = " + userQuestion +
        ", aiReply = " + aiReply +
        ", tokensUsed = " + tokensUsed +
        ", createdTime = " + createdTime +
        "}";
    }
}
