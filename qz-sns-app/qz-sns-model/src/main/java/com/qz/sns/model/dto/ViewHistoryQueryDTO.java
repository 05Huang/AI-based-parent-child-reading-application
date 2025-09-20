package com.qz.sns.model.dto;

import lombok.Data;

/**
 * 浏览历史查询DTO
 */
@Data
public class ViewHistoryQueryDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 内容类型
     */
    private Integer contentType;

    /**
     * 分类ID
     */
    private Long categoryId;

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