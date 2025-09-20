package com.qz.sns.sv.util;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.google.gson.Gson;
import com.qz.sns.sv.config.props.AliyunSmsProperties;
import darabonba.core.client.ClientOverrideConfiguration;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
 
/**
 * 短信发送工具类
 */
@Component

public class SMSUtils {

    @Autowired
	private  AliyunSmsProperties aliyunSmsProperties;
 
	/**
	 * 发送短信
	 * @param phoneNumbers 手机号
	 * @param param 参数
	 */
	public  boolean sendMessage( String phoneNumbers, Map<String, Object> param) throws Exception {
 
		StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
				.accessKeyId("LTAI5tM59apoUsDjGqmG5eYb") //注意将此处AccessKeyId替换为2.3中保存的子用户的AccessKeyId
				.accessKeySecret("D7txrDw3VvqCq23nAu8ioNaNL0RMfu")//注意将此处AccessKeySecret替换为2.3中保存的字用户的AccessKeySecret
				.build());
 
		AsyncClient client = AsyncClient.builder()
				.region("cn-hangzhou") // Region ID
				.credentialsProvider(provider)
				.overrideConfiguration(
						ClientOverrideConfiguration.create()
								.setEndpointOverride("dysmsapi.aliyuncs.com") // Endpoint
				)
				.build();
		System.out.println(param.get("code")+phoneNumbers);
		SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
				.signName("亲子阅读APP")
				.templateCode("SMS_474930088")
				.phoneNumbers(phoneNumbers)
				//模板参数包括验证码和过期时间
				.templateParam("{\"code\":\""+param.get("code")+"\"}")
				.build();
 
		CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
		SendSmsResponse resp = response.get();
		System.out.println(new Gson().toJson(resp.getBody()));
 
		client.close();
		return true;
	}


}