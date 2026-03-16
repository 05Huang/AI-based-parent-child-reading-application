package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class RecommendationRequestDTO {
    private Long userId;
    private Integer size = 10; // 默认推荐10个
    private Integer type; // 1=图文,2=视频, null=全部
    private Boolean shuffle; // 是否打乱用于“换一换”
}
