package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("ai_bot_config")
@ApiModel(value = "AiBotConfig对象", description = "AI机器人配置表")
public class AiBotConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("AI机器人用户ID")
    private Long botUserId;

    @ApiModelProperty("机器人名称")
    private String botName;

    @ApiModelProperty("机器人头像")
    private String botAvatar;

    @ApiModelProperty("AI模型类型")
    private String modelType;

    @ApiModelProperty("API密钥")
    private String apiKey;

    @ApiModelProperty("最大tokens")
    private Integer maxTokens;

    @ApiModelProperty("温度参数")
    private java.math.BigDecimal temperature;

    @ApiModelProperty("系统提示词")
    private String systemPrompt;

    @ApiModelProperty("是否启用 0:禁用 1:启用")
    private Boolean enabled;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBotUserId() {
        return botUserId;
    }

    public void setBotUserId(Long botUserId) {
        this.botUserId = botUserId;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getBotAvatar() {
        return botAvatar;
    }

    public void setBotAvatar(String botAvatar) {
        this.botAvatar = botAvatar;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public java.math.BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(java.math.BigDecimal temperature) {
        this.temperature = temperature;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
        return "AiBotConfig{" +
                "id=" + id +
                ", botUserId=" + botUserId +
                ", botName='" + botName + '\'' +
                ", botAvatar='" + botAvatar + '\'' +
                ", modelType='" + modelType + '\'' +
                ", apiKey='***'" +
                ", maxTokens=" + maxTokens +
                ", temperature=" + temperature +
                ", systemPrompt='" + systemPrompt + '\'' +
                ", enabled=" + enabled +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}

