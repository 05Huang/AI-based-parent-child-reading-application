package com.qz.sns.model.dto;

import com.qz.sns.common.utils.BeanUtils;
import com.qz.sns.model.entity.Comment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 评论数据传输对象
 * 包含评论基本信息和关联的用户信息
 */
@Data
public class CommentDTO {

        private Long id;
        private Long contentId;
        private Long userId;
        private Long parentId;
        private Long rootId;
        private String content;
        private Integer likeCount;
        private Integer replyCount;
        private Integer status;
        private Date createdTime;
        private Integer commentType;
        private String paragraphId;

        // 额外字段
        private String userName;
        private String userAvatar;
        private String replyToName;
        private Boolean isLiked;
        private List<CommentDTO> replies;
    }

