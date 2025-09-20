package com.qz.sns.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 简化的评论响应DTO
 */
@Data
public class SimpleCommentResponseDTO {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 内容ID
     */
    private Long contentId;
    
    /**
     * 内容标题
     */
    private String contentTitle;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 父评论ID
     */
    private Long parentId;
    
    /**
     * 根评论ID
     */
    private Long rootId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 回复数
     */
    private Integer replyCount;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 评论类型：1-普通评论，2-段落评论
     */
    private Integer commentType;
    
    /**
     * 段落ID
     */
    private String paragraphId;
}