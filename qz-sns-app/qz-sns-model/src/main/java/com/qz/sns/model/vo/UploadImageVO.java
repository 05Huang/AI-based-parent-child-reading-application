package com.qz.sns.model.vo;

import lombok.Data;

@Data
public class UploadImageVO {
    /**
     * 原图URL
     */
    private String originUrl;

    /**
     * 缩略图URL（如果有）
     */
    private String thumbUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;
}