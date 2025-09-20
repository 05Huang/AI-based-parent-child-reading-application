package com.qz.sns.sv.service;

import com.qz.sns.model.dto.ContentRequest;

import java.util.List;

public interface RecommendationService {
    /**
     * 获取推荐内容列表
     * @param userId 用户ID
     * @param size 推荐数量
     * @return 推荐内容列表
     */
    List<ContentRequest> getRecommendedContents(Long userId, Integer size);

    /**
     * 获取热门内容
     * @param size 数量
     * @return 热门内容列表
     */
    List<ContentRequest> getHotContents(Integer size);

    /**
     * 更新用户行为
     * @param userId 用户ID
     */
    void updateUserBehavior(Long userId);
}