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
 * 问卷星问卷信息表
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
@Data
@TableName("survey_star_questionnaire")
@ApiModel(value = "SurveyStarQuestionnaire对象", description = "问卷星问卷信息表")
public class SurveyStarQuestionnaire implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("问卷星问卷ID")
    private Long questId;

    @ApiModelProperty("问卷标题")
    private String title;

    @ApiModelProperty("问卷描述")
    private String description;

    @ApiModelProperty("问卷类型：reading-阅读调查 feedback-用户反馈 experience-功能体验 other-其他")
    private String type;

    @ApiModelProperty("状态：1-进行中 2-已结束 3-未发布")
    private Integer status;

    @ApiModelProperty("回答数")
    private Integer answerCount;

    @ApiModelProperty("问卷链接")
    private String surveyUrl;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;

    @ApiModelProperty("备注")
    private String remark;
}
