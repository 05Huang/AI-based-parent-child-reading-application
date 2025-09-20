package com.qz.sns.common.constant;

/**
 * @Author: 李彬
 * @Date: 2025/3/19 下午2:40
 * @Version: v1.0.0
 * @Description: TODO
 **/
public class UserConstant {
    //redis中用户信息的key
    public static final String REDIS_SMS_CODE = "SMS_CODE:" ;
    public static final String REDIS_SMS_LIMIT = "SMS_SEND_LIMIT:";
    public static final Integer SMS_CODE_EXPIRE_TIME = 300;
    public static final Integer SMS_CODE_RESEND_LIMIT = 60;
}
