package com.qz.sns.model.dto;

import lombok.Data;

@Data
public class LikeDTO {
    private Long userId;
    private Long targetId;
    private Integer type; // 1: 内容，2: 评论
}