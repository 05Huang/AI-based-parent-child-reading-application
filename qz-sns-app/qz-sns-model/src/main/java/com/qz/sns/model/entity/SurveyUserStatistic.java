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
 * 用户问卷参与统计表
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
@Data
@TableName("survey_user_statistic")
@ApiModel(value = "SurveyUserStatistic对象", description = "用户问卷参与统计表")
public class SurveyUserStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("参与总数")
    private Integer totalSurveys;

    @ApiModelProperty("已完成数")
    private Integer completedSurveys;

    @ApiModelProperty("获得的总积分")
    private Integer totalPoints;

    @ApiModelProperty("最后参与时间")
    private LocalDateTime lastSurveyTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;
}


