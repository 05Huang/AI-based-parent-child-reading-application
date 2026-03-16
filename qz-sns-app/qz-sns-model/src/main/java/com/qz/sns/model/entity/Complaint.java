package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("im_complaint")
@ApiModel(value = "Complaint对象", description = "投诉")
public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("投诉人用户ID")
    private Long complainantUserId;

    @ApiModelProperty("群ID")
    private Long groupId;

    @ApiModelProperty("对象类型 1:群")
    private Integer targetType;

    @ApiModelProperty("对象ID")
    private Long targetId;

    @ApiModelProperty("投诉内容")
    private String content;

    @ApiModelProperty("处理状态 0:待处理 1:已处理")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty("处理人")
    private Long handledBy;

    @ApiModelProperty("处理时间")
    private LocalDateTime handledTime;

    @ApiModelProperty("处理结果")
    private String result;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getComplainantUserId() { return complainantUserId; }
    public void setComplainantUserId(Long complainantUserId) { this.complainantUserId = complainantUserId; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public Long getHandledBy() { return handledBy; }
    public void setHandledBy(Long handledBy) { this.handledBy = handledBy; }
    public LocalDateTime getHandledTime() { return handledTime; }
    public void setHandledTime(LocalDateTime handledTime) { this.handledTime = handledTime; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}

