package com.qz.sns.sv.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient {

    @Value("${ai.deepseek.api-key:}")
    private String apiKey;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String model;

    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();

    public ChatResult chat(String systemPrompt, String question, Integer maxTokens, Double temperature) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("DeepSeek API key is not configured");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
        }
        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", question);
        messages.add(user);
        body.put("messages", messages);
        body.put("stream", false);
        if (maxTokens != null && maxTokens > 0) {
            body.put("max_tokens", maxTokens);
        }
        if (temperature != null) {
            body.put("temperature", temperature);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String json = restTemplate.postForObject(API_URL, entity, String.class);
        if (json == null || json.isEmpty()) {
            throw new IllegalStateException("Empty response from DeepSeek API");
        }
        JSONObject obj = JSON.parseObject(json);
        JSONArray choices = obj.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("Invalid response from DeepSeek API");
        }
        JSONObject first = choices.getJSONObject(0);
        JSONObject message = first.getJSONObject("message");
        String content = message != null ? message.getString("content") : null;
        JSONObject usage = obj.getJSONObject("usage");
        Integer tokens = null;
        if (usage != null) {
            tokens = usage.getInteger("total_tokens");
        }
        ChatResult result = new ChatResult();
        result.setReply(content != null ? content.trim() : "");
        result.setTokensUsed(tokens != null ? tokens : 0);
        return result;
    }

    public static class ChatResult {
        private String reply;
        private int tokensUsed;

        public String getReply() {
            return reply;
        }

        public void setReply(String reply) {
            this.reply = reply;
        }

        public int getTokensUsed() {
            return tokensUsed;
        }

        public void setTokensUsed(int tokensUsed) {
            this.tokensUsed = tokensUsed;
        }
    }
}

