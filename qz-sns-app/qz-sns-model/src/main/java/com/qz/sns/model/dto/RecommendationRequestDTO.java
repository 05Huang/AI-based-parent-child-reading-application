package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class RecommendationRequestDTO {
    private Long userId;
    private Integer size = 10; // 默认推荐10个
}