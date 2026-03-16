package com.qz.sns.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 问卷星数据推送DTO
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
@Data
@ApiModel(value = "SurveyDataPushDTO对象", description = "问卷星数据推送DTO")
public class SurveyDataPushDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("答卷ID")
    private Long id;

    @ApiModelProperty("问卷ID")
    private Long questid;

    @ApiModelProperty("用户ID（来自自定义参数）")
    private String userid;

    @ApiModelProperty("答案数据（JSON格式）")
    private String answer;

    @ApiModelProperty("提交IP")
    private String ip;

    @ApiModelProperty("提交时间戳")
    private Long submittime;
}


