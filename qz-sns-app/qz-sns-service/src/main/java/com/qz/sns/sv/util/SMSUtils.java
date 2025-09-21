package com.qz.sns.sv.util;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

/**
 * 短信发送工具类
 */
@Slf4j
@Component
public class SMSUtils {
    
    private static final String SPUG_API_URL = "https://push.spug.cc/send/6dwezjV9d3mgLkRG";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送短信
     * @param phoneNumbers 手机号
     * @param param 参数
     */
    public boolean sendMessage(String phoneNumbers, Map<String, Object> param) throws Exception {
        try {
            log.info("开始发送短信验证码，手机号：{}", phoneNumbers);
            
            // 构建请求参数
            String code = param.get("code").toString();
            String requestUrl = String.format("%s?name=%s&code=%s&targets=%s",
                SPUG_API_URL,
                "亲子阅读平台",
                code,
                phoneNumbers
            );
            
            log.info("发送短信验证码请求URL：{}", requestUrl);
            
            // 发送GET请求
            String response = restTemplate.getForObject(requestUrl, String.class);
            
            log.info("短信验证码发送响应：{}", response);
            
            return true;
        } catch (Exception e) {
            log.error("发送短信验证码失败：{}", e.getMessage(), e);
            throw new Exception("短信发送失败：" + e.getMessage());
        }
    }
}