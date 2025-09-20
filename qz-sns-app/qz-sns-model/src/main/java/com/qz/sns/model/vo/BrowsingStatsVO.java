package com.qz.sns.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BrowsingStatsVO {
    private Long totalReadDuration; // 阅读总时长(秒)
    private Long totalReadCount; // 阅读总数
    private Long weeklyViewCount; // 本周浏览数
    private BigDecimal interactionRate; // 互动频率
}
