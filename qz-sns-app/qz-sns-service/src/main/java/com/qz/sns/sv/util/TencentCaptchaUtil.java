package com.qz.sns.sv.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯云验证码校验工具类
 */
@Slf4j
@Component
public class TencentCaptchaUtil {
    
    // 腾讯云验证码配置
    @Value("${tencent.captcha.app-id}")
    private String captchaAppId;

    @Value("${tencent.captcha.app-secret-key}")
    private String appSecretKey;

    @Value("${tencent.captcha.enabled:false}")
    private boolean enableCaptchaVerify;

    private static final String VERIFY_URL = "https://ssl.captcha.qq.com/ticket/verify";
    
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 校验腾讯云验证码票据
     * @param ticket 验证码返回的票据
     * @param randstr 验证码返回的随机串
     * @param userIp 用户IP地址
     * @return 是否校验成功
     */
    public boolean verifyCaptcha(String ticket, String randstr, String userIp) {
        try {
            log.info("开始校验腾讯云验证码，ticket：{}，randstr：{}，userIp：{}", ticket, randstr, userIp);
            
            // 如果禁用了验证码校验，直接返回true（仅用于开发测试）
            if (!enableCaptchaVerify) {
                log.warn("⚠️ 验证码校验已禁用（tencent.captcha.enabled=false），直接返回成功。生产环境请启用！");
                return true;
            }
            
            // 如果票据或随机串为空，直接返回false
            if (ticket == null || ticket.isEmpty() || randstr == null || randstr.isEmpty()) {
                log.warn("验证码票据或随机串为空");
                return false;
            }
            
            // 构建请求URL（注意：参数名需要严格匹配腾讯云要求）
            String finalUserIp = (userIp != null && !userIp.equals("0:0:0:0:0:0:0:1")) ? userIp : "127.0.0.1";
            
            String requestUrl = String.format(
                "%s?aid=%s&AppSecretKey=%s&Ticket=%s&Randstr=%s&UserIP=%s",
                VERIFY_URL,
                captchaAppId,
                appSecretKey,
                ticket,
                randstr,
                finalUserIp
            );
            
            log.info("腾讯云验证码校验请求URL（隐藏密钥）：{}?aid={}&Ticket={}&Randstr={}&UserIP={}", 
                    VERIFY_URL, captchaAppId, ticket, randstr, finalUserIp);
            
            // 发送GET请求
            String response = restTemplate.getForObject(requestUrl, String.class);
            log.info("腾讯云验证码校验响应：{}", response);
            
            // 解析响应
            JSONObject jsonObject = JSON.parseObject(response);
            Integer responseCode = jsonObject.getInteger("response");
            String evilLevel = jsonObject.getString("evil_level");
            String errMsg = jsonObject.getString("err_msg");
            
            // response=1 表示验证成功
            // response=0 表示验证失败
            // response=100 表示参数错误（appid-secretkey-ticket mismatch）
            if (responseCode != null && responseCode == 1) {
                log.info("✅ 腾讯云验证码校验成功，evil_level：{}", evilLevel);
                return true;
            } else {
                log.warn("❌ 腾讯云验证码校验失败，responseCode：{}，errMsg：{}", responseCode, errMsg);
                if (responseCode != null && responseCode == 100) {
                    log.error("错误码100：AppId、AppSecretKey或Ticket不匹配，请检查配置！");
                    log.error("当前AppId：{}，请到腾讯云控制台确认AppSecretKey是否正确", captchaAppId);
                }
                return false;
            }
            
        } catch (Exception e) {
            log.error("腾讯云验证码校验异常：{}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 从请求中获取用户真实IP地址
     * @param request HTTP请求对象
     * @return 用户IP地址
     */
    public static String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于多级代理的情况，第一个IP为真实IP，多个IP以逗号分隔
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        log.debug("获取到客户端IP：{}", ip);
        return ip != null ? ip : "127.0.0.1";
    }
}

