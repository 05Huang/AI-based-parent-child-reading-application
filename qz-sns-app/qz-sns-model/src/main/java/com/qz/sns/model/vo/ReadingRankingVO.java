package com.qz.sns.model.vo;

import lombok.Data;

/**
 * 阅读排行榜VO
 */
@Data
public class ReadingRankingVO {
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 本周阅读时长（秒）
     */
    private Long readDuration;
    
    /**
     * 排名变化趋势：up(上升)、down(下降)、equal(持平)
     */
    private String trend;
    
    /**
     * 排名变化数量
     */
    private Integer trendNumber;
}





