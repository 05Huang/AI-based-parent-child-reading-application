package com.qz.sns.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author: 李彬
 * @Date: 2025/5/10 下午7:44
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Data
public class ContentDTO {


    private String title;
    private String content;
    private Integer type;
    private String coverUrl;
    private String mediaUrl;
    private Long categoryId;
    private String tags;
    private Integer contentLength; // 内容长度(字数/秒数)
    private List<String> imageUrls;

}
