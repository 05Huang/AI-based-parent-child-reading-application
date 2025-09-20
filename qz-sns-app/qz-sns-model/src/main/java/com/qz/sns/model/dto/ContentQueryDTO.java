package com.qz.sns.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 内容查询请求DTO
 */
@Data
public class ContentQueryDTO {

    /**
     * 标题关键词
     */
    private String title;

    /**
     * 内容类型：1-图文，2-视频
     */
    @Min(value = 1, message = "内容类型值范围为1-2")
    @Max(value = 2, message = "内容类型值范围为1-2")
    private Integer type;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签关键词
     */
    private String tags;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 状态：1-正常，0-下架，null-查询所有状态（默认查询正常状态）
     */
    @Min(value = 0, message = "状态值只能为0或1")
    @Max(value = 1, message = "状态值只能为0或1")
    private Integer status;

    /**
     * 最小浏览数
     */
    @Min(value = 0, message = "最小浏览数不能小于0")
    private Integer minViewCount;

    /**
     * 最大浏览数
     */
    @Min(value = 0, message = "最大浏览数不能小于0")
    private Integer maxViewCount;

    /**
     * 最小点赞数
     */
    @Min(value = 0, message = "最小点赞数不能小于0")
    private Integer minLikeCount;

    /**
     * 最大点赞数
     */
    @Min(value = 0, message = "最大点赞数不能小于0")
    private Integer maxLikeCount;

    /**
     * 最小热度得分
     */
    @DecimalMin(value = "0.0", message = "最小热度得分不能小于0")
    private BigDecimal minPopularityScore;

    /**
     * 最大热度得分
     */
    @DecimalMin(value = "0.0", message = "最大热度得分不能小于0")
    private BigDecimal maxPopularityScore;

    /**
     * 创建时间开始
     */
    private String startTime;

    /**
     * 创建时间结束
     */
    private String endTime;

    /**
     * 排序字段：view_count, like_count, created_time, popularity_score, updated_time
     */
    private String sortField;

    /**
     * 排序方向：asc, desc
     */
    private String sortOrder;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;

    /**
     * 获取状态值，如果为null则默认返回1（正常状态）
     */
    public Integer getEffectiveStatus() {
        return status != null ? status : 1;
    }

    /**
     * 是否查询所有状态
     */
    public boolean isQueryAllStatus() {
        return status == null;
    }
}