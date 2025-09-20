package com.qz.sns.model.dto;

import lombok.Data;

/**
 * 简化的评论查询DTO
 */
@Data
public class SimpleCommentQueryDTO {

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评论内容关键词
     */
    private String content;

    /**
     * 状态：1-正常，0-删除，null-查询所有状态（默认查询正常状态）
     */
    private Integer status;

    /**
     * 评论类型：1-普通评论，2-段落评论
     */
    private Integer commentType;

    /**
     * 段落ID
     */
    private String paragraphId;

    /**
     * 创建时间开始
     */
    private String startTime;

    /**
     * 创建时间结束
     */
    private String endTime;

    /**
     * 排序字段：created_time, like_count
     */
    private String sortField;

    /**
     * 排序方向：asc, desc
     */
    private String sortOrder;

    /**
     * 页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}