package com.qz.sns.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeeklyReportVO {
    private Long totalReadDuration; // 浏览总时长
    private Long totalArticleCount; // 浏览文章数
    private Long interactionCount; // 互动次数
    private BigDecimal activityScore; // 活跃度
    private BigDecimal dailyAvgReadTime; // 每日平均阅读时间
    private String favoriteCategory; // 最感兴趣的阅读分类
}