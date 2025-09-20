package com.qz.sns.model.vo;

import lombok.Data;

@Data
public class HistoryStatsVO {
    private Long monthlyReadDuration; // 本月浏览时长
    private Long totalArticleCount; // 浏览文章总数
    private Long interactedArticleCount; // 互动文章数
    private Long shareCount; // 分享次数
}