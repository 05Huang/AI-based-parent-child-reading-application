package com.qz.sns.sv.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.qz.sns.common.utils.BeanUtils;
import com.qz.sns.model.dto.ContentRequest;
import com.qz.sns.model.entity.Content;
import com.qz.sns.sv.mapper.ContentMapper;
import com.qz.sns.sv.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class RecommendationServiceImpl implements RecommendationService {
    
    @Autowired
    private ContentMapper contentMapper;

    private static final String PYTHON_HOT_CONTENTS_URL = "http://127.0.0.1:5000/get_hot_contents";
    private static final String PYTHON_HOT_ARTICLES_URL = "http://127.0.0.1:5000/get_hot_articles";
    private static final String PYTHON_UPDATE_BEHAVIOR_URL = "http://127.0.0.1:5000/update_user_behavior";
    private static final String PYTHON_RECOMMENDATION_URL = "http://127.0.0.1:5000/get_recommendations";
    private static final String PYTHON_ARTICLE_RECOMMENDATION_URL = "http://127.0.0.1:5000/get_article_recommendations";
    
    /**
     * 获取推荐内容列表
     */
    @Override
    public List<ContentRequest> getRecommendedContents(Long userId, Integer size) {
        try {
            // 1. 调用 Python 接口获取推荐内容ID列表
            List<Long> recommendedIds = getRecommendationIds(userId, size);
            
            if (recommendedIds == null || recommendedIds.isEmpty()) {
                log.info("No recommendations found for user: {}", userId);
                return new ArrayList<>();
            }
            
            // 2. 批量查询推荐内容
            List<Content> contents = contentMapper.selectBatchIds(recommendedIds);
            
            // 3. 更新推荐相关字段
            updateRecommendationFields(recommendedIds);
            
            // 4. 转换为 DTO
            List<ContentRequest> recommendedContents = new ArrayList<>();
            for (Content content : contents) {
                ContentRequest dto = convertToContentRequest(content);
                recommendedContents.add(dto);
            }
            
            // 5. 按推荐顺序排序
            Map<Long, Integer> orderMap = new HashMap<>();
            for (int i = 0; i < recommendedIds.size(); i++) {
                orderMap.put(recommendedIds.get(i), i);
            }
            recommendedContents.sort((a, b) -> {
                Integer orderA = orderMap.getOrDefault(a.getId(), Integer.MAX_VALUE);
                Integer orderB = orderMap.getOrDefault(b.getId(), Integer.MAX_VALUE);
                return orderA.compareTo(orderB);
            });
            
            return recommendedContents;
            
        } catch (Exception e) {
            log.error("Error getting recommended contents for user: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 调用 Python 接口获取推荐内容ID列表
     */
    private List<Long> getRecommendationIds(Long userId, Integer size) throws Exception {
        // 构建请求
        JSONObject request = new JSONObject();
        request.put("user_id", userId);
        request.put("size", size != null ? size : 10);
        
        log.info("Calling Python recommendation API for user: {}, size: {}", userId, size);
        
        // 发送请求
        String response = sendPostRequest(PYTHON_RECOMMENDATION_URL, request.toString());
        
        // 解析响应
        JSONObject responseJson = JSON.parseObject(response);
        if (responseJson.getBoolean("success")) {
            JSONArray recommendationsArray = responseJson.getJSONArray("recommendations");
            List<Long> recommendations = new ArrayList<>();
            for (int i = 0; i < recommendationsArray.size(); i++) {
                recommendations.add(recommendationsArray.getLong(i));
            }
            log.info("Received {} recommendations for user: {}", recommendations.size(), userId);
            return recommendations;
        } else {
            log.error("Failed to get recommendations: {}", responseJson.getString("error"));
            return new ArrayList<>();
        }
    }
    
    /**
     * 更新推荐相关字段
     */
    private void updateRecommendationFields(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 批量更新推荐相关字段
        for (Long contentId : contentIds) {
            Content updateContent = new Content();
            updateContent.setId(contentId);
            updateContent.setLastRecommended(now);
            
            // 使用 UpdateWrapper 更新推荐次数
            UpdateWrapper<Content> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", contentId)
                        .set("last_recommended", now)
                        .setSql("recommended_count = recommended_count + 1");
            
            contentMapper.update(null, updateWrapper);
        }
        
        log.info("Updated recommendation fields for {} contents", contentIds.size());
    }
    
    /**
     * 转换为 ContentRequest DTO
     */
    private ContentRequest convertToContentRequest(Content content) {
        ContentRequest dto = new ContentRequest();
        BeanUtils.copyProperties(content, dto);
        return dto;
    }
    
    /**
     * 发送 POST 请求到 Python 服务
     */
    private String sendPostRequest(String urlString, String jsonInputString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        // 发送请求
        try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(jsonInputString);
            writer.flush();
        }

        // 检查响应代码
        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder errorResponse = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new RuntimeException("Server returned error: " + responseCode + ", body: " + errorResponse.toString());
            }
        }

        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    /**
     * 获取热门内容
     */
    @Override
    public List<ContentRequest> getHotContents(Integer size) {
        try {
            // 调用 Python 接口
            String url = PYTHON_HOT_CONTENTS_URL + "?size=" + size;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // 读取响应
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

            // 解析响应
            JSONObject responseJson = JSON.parseObject(response.toString());
            if (responseJson.getBoolean("success")) {
                JSONArray hotContentsArray = responseJson.getJSONArray("hot_contents");
                List<Long> contentIds = new ArrayList<>();
                for (int i = 0; i < hotContentsArray.size(); i++) {
                    contentIds.add(hotContentsArray.getLong(i));
                }

                // 查询内容详情
                if (!contentIds.isEmpty()) {
                    List<Content> contents = contentMapper.selectBatchIds(contentIds);
                    return contents.stream()
                            .map(this::convertToContentRequest)
                            .collect(Collectors.toList());
                }
            }

            return new ArrayList<>();

        } catch (Exception e) {
            log.error("Error getting hot contents", e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新用户行为
     */
    @Override
    public void updateUserBehavior(Long userId) {
        try {
            JSONObject request = new JSONObject();
            request.put("user_id", userId);

            String response = sendPostRequest(PYTHON_UPDATE_BEHAVIOR_URL, request.toString());

            JSONObject responseJson = JSON.parseObject(response);
            if (!responseJson.getBoolean("success")) {
                log.error("Failed to update user behavior: {}", responseJson.getString("error"));
            }

        } catch (Exception e) {
            log.error("Error updating user behavior for user: {}", userId, e);
            throw new RuntimeException("更新用户行为失败", e);
        }
    }

    /**
     * 获取文章推荐（图文内容）
     */
    @Override
    public List<ContentRequest> getArticleRecommendations(Long userId, Integer size) {
        try {
            log.info("===== 开始获取文章推荐 =====");
            log.info("用户ID: {}, 请求数量: {}", userId, size);
            
            // 1. 调用 Python 接口获取推荐文章ID列表
            JSONObject request = new JSONObject();
            request.put("user_id", userId);
            request.put("size", size != null ? size : 10);
            
            log.info("调用Python推荐API: {}", PYTHON_ARTICLE_RECOMMENDATION_URL);
            log.info("请求参数: {}", request.toString());
            
            String response = sendPostRequest(PYTHON_ARTICLE_RECOMMENDATION_URL, request.toString());
            
            log.info("Python API响应: {}", response);
            
            // 2. 解析响应
            JSONObject responseJson = JSON.parseObject(response);
            if (responseJson.getBoolean("success")) {
                JSONArray recommendationsArray = responseJson.getJSONArray("recommendations");
                List<Long> recommendedIds = new ArrayList<>();
                for (int i = 0; i < recommendationsArray.size(); i++) {
                    recommendedIds.add(recommendationsArray.getLong(i));
                }
                
                log.info("收到推荐文章ID列表: {}", recommendedIds);
                
                if (recommendedIds.isEmpty()) {
                    log.info("Python返回空推荐列表");
                    return new ArrayList<>();
                }
                
                // 3. 批量查询推荐内容
                List<Content> contents = contentMapper.selectBatchIds(recommendedIds);
                log.info("从数据库查询到 {} 篇文章", contents.size());
                
                // 4. 更新推荐相关字段
                updateRecommendationFields(recommendedIds);
                
                // 5. 转换为 DTO
                List<ContentRequest> recommendedContents = new ArrayList<>();
                for (Content content : contents) {
                    ContentRequest dto = convertToContentRequest(content);
                    recommendedContents.add(dto);
                    log.info("推荐文章: ID={}, 标题={}", dto.getId(), dto.getTitle());
                }
                
                // 6. 按推荐顺序排序
                Map<Long, Integer> orderMap = new HashMap<>();
                for (int i = 0; i < recommendedIds.size(); i++) {
                    orderMap.put(recommendedIds.get(i), i);
                }
                recommendedContents.sort((a, b) -> {
                    Integer orderA = orderMap.getOrDefault(a.getId(), Integer.MAX_VALUE);
                    Integer orderB = orderMap.getOrDefault(b.getId(), Integer.MAX_VALUE);
                    return orderA.compareTo(orderB);
                });
                
                log.info("===== 文章推荐完成，返回 {} 篇文章 =====", recommendedContents.size());
                return recommendedContents;
                
            } else {
                String error = responseJson.getString("error");
                log.error("Python API返回失败: {}", error);
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("获取文章推荐失败", e);
            log.error("异常信息: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取热门文章（图文内容）
     */
    @Override
    public List<ContentRequest> getHotArticles(Long userId, Integer size) {
        try {
            log.info("===== 开始获取热门文章 =====");
            log.info("用户ID: {}, 请求数量: {}", userId, size);
            
            // 调用 Python 接口
            String url = PYTHON_HOT_ARTICLES_URL + "?size=" + (size != null ? size : 10);
            if (userId != null) {
                url += "&user_id=" + userId;
            }
            
            log.info("调用Python热门文章API: {}", url);
            
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // 读取响应
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

            log.info("Python API响应: {}", response.toString());

            // 解析响应
            JSONObject responseJson = JSON.parseObject(response.toString());
            if (responseJson.getBoolean("success")) {
                JSONArray hotContentsArray = responseJson.getJSONArray("hot_contents");
                List<Long> contentIds = new ArrayList<>();
                for (int i = 0; i < hotContentsArray.size(); i++) {
                    contentIds.add(hotContentsArray.getLong(i));
                }

                log.info("收到热门文章ID列表: {}", contentIds);

                // 查询内容详情
                if (!contentIds.isEmpty()) {
                    List<Content> contents = contentMapper.selectBatchIds(contentIds);
                    log.info("从数据库查询到 {} 篇热门文章", contents.size());
                    
                    List<ContentRequest> result = contents.stream()
                            .map(content -> {
                                ContentRequest dto = convertToContentRequest(content);
                                log.info("热门文章: ID={}, 标题={}, 浏览量={}", 
                                    dto.getId(), dto.getTitle(), dto.getViewCount());
                                return dto;
                            })
                            .collect(Collectors.toList());
                    
                    log.info("===== 热门文章获取完成，返回 {} 篇文章 =====", result.size());
                    return result;
                }
            } else {
                String error = responseJson.getString("error");
                log.error("Python API返回失败: {}", error);
            }

            return new ArrayList<>();

        } catch (Exception e) {
            log.error("获取热门文章失败", e);
            log.error("异常信息: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}