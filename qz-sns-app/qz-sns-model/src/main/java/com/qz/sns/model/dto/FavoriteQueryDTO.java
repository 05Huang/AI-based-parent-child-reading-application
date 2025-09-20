package com.qz.sns.model.dto;

import lombok.Data;

/**
 * 收藏查询请求DTO
 */
@Data
public class FavoriteQueryDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 收藏备注关键词
     */
    private String note;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;
}