package com.qz.sns.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户行为请求DTO
 */
@Data
public class UserBehaviorDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 行为类型：view,like,comment,share,collect
     */
    private String behaviorType;
    
    /**
     * 行为持续时间(秒)
     */
    private Integer duration;
    
    /**
     * 进度百分比(0-100)
     */
    private BigDecimal progress;
    
    /**
     * 来源：recommend,search,follow,hot等
     */
    private String source;
}