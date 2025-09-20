package com.qz.sns.model.models;

import lombok.Data;

@Data
public class IntimacyScore {
    private Long userId;
    private Long relativeId;
    private String relationType;
    private String nickname;
    private String avatar;
    private Double score;
    private Double percentage;
    private Integer rank;
}