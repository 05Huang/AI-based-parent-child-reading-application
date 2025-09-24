package com.qz.sns.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户浏览历史响应DTO
 */
@Data
public class UserViewHistoryResponseDTO {
    
    /**
     * 浏览记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 内容标题
     */
    private String contentTitle;
    
    /**
     * 内容封面
     */
    private String coverUrl;
    
    /**
     * 内容类型：1-图文，2-视频
     */
    private Integer contentType;
    
    /**
     * 内容分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 创建者昵称
     */
    private String creatorName;
    
    /**
     * 浏览时间
     */
    private LocalDateTime viewTime;
    
    /**
     * 内容状态（用于判断内容是否还存在）
     */
    private Integer contentStatus;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
}
