package com.qz.sns.sv.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    /**
     * MinIO服务地址
     */
    private String endpoint;

    /**
     * 访问域名（用于生成文件URL）
     */
    private String domain;

    /**
     * Access Key
     */
    private String accessKey;

    /**
     * Secret Key
     */
    private String secretKey;

    /**
     * 默认存储桶
     */
    private String bucketName;

    /**
     * 图片存储路径
     */
    private String imagePath;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 视频存储路径
     */
    private String videoPath;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
