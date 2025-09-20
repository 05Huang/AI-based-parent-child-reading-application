package com.qz.sns.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VideoUploadRequest {
    private Long id;
    private String title;
    private String content;
    private Integer type; // 1: 图文，2: 视频
    private String coverUrl;
    private String mediaUrl; // 上传后自动填充
    @JsonProperty("category_id")
    private Long categoryId;
    private List<String> tags;
    @JsonProperty("creator_id")
    private Long creatorId;
}
