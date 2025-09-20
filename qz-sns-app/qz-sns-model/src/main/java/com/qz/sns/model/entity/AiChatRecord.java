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

    @ApiModelProperty("对话内容")
    private String content;

    @ApiModelProperty("AI分析建议")
    private String analysis;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
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
        ", content = " + content +
        ", analysis = " + analysis +
        ", createdTime = " + createdTime +
        "}";
    }
}
