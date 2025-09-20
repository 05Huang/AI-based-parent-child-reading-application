package com.qz.sns.model.dto;


import com.qz.sns.model.entity.ContentImage;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ContentRequest {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private String coverUrl;
    private String mediaUrl;
    private Long categoryId;
    private String tags;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer status;
    private Long creatorId;
    private String creatorName;
    private String creatorAvatar;
    private Date createdTime;
    private Date updatedTime;
    private List<ContentImage> images;
}
