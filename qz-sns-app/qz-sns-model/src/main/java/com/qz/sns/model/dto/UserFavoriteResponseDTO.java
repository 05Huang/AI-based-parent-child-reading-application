package com.qz.sns.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户收藏内容响应DTO
 */
@Data
public class UserFavoriteResponseDTO {

    /**
     * 收藏ID
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
     * 收藏备注
     */
    private String note;

    /**
     * 收藏时间
     */
    private LocalDateTime createdTime;

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
     * 内容状态（用于判断内容是否还存在）
     */
    private Integer contentStatus;
}