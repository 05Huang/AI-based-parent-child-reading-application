package com.qz.sns.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 问卷星答卷记录表
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
@Data
@TableName("survey_star_answer")
@ApiModel(value = "SurveyStarAnswer对象", description = "问卷星答卷记录表")
public class SurveyStarAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("问卷星答卷ID")
    private String answerId;

    @ApiModelProperty("问卷ID")
    private Long questId;

    @ApiModelProperty("用户ID（来自自定义参数）")
    private String userId;

    @ApiModelProperty("答案数据（JSON格式）")
    private String answerData;

    @ApiModelProperty("提交IP")
    private String submitIp;

    @ApiModelProperty("提交时间")
    private LocalDateTime submitTime;

    @ApiModelProperty("数据接收时间")
    private LocalDateTime receivedTime;

    @ApiModelProperty("状态：1-已保存 2-已处理 3-已分析")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}


