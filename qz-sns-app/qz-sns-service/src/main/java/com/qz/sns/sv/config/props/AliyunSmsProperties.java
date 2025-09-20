package com.qz.sns.sv.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信属性
 *
 * @author qiangesoft
 * @date 2024-04-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")//配置文件中的前缀
public class AliyunSmsProperties {



    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 地域id
     */
    private String regionId;

    /**
     * accessKeyId
     */
    private String accessKeyId;

    /**
     * accessKeySecret
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 模板code
     */
    private String templateCode;

}

