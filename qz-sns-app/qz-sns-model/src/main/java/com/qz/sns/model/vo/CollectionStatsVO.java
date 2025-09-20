package com.qz.sns.model.vo;

import lombok.Data;

@Data
public class CollectionStatsVO {
    private Long totalCollections; // 收藏总数
    private Long monthlyCollections; // 本月收藏
    private Long collectionShares; // 收藏分享数
    private Long interactionCount; // 互动数
}