package com.qz.sns.web.controller;


import com.qz.sns.model.vo.IntimacyRankingVO;
import com.qz.sns.sv.service.impl.IntimacyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/intimacy")
public class IntimacyController {

    @Autowired
    private IntimacyService intimacyService;

    /**
     * 获取全网亲密度排行
     */
    @GetMapping("/global-ranking")
    public Map<String, Object> getGlobalRanking() {
        List<IntimacyRankingVO> ranking = intimacyService.getGlobalRanking();
        Map<String, Object> result = new HashMap<>();
        result.put("ranking", ranking);
        return result;
    }

    /**
     * 获取用户家庭亲密度排行
     */
    @GetMapping("/user-ranking/{userId}")
    public Map<String, Object> getUserRanking(@PathVariable Long userId) {
        // 获取用户家庭内排行
        List<IntimacyRankingVO> ranking = intimacyService.getUserRanking(userId);
        
        // 获取用户在全网的排名和亲密度
        Map<String, Object> globalInfo = intimacyService.getUserGlobalInfo(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("ranking", ranking);
        result.put("globalRank", globalInfo.get("globalRank"));
        result.put("intimacyPercentage", globalInfo.get("intimacyPercentage"));
        
        return result;
    }

    /**
     * 手动触发亲密度计算（用于测试）
     */
    @PostMapping("/calculate")
    public Map<String, Object> calculateIntimacy() {
        intimacyService.calculateAndSaveAllIntimacyScores();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "亲密度计算已触发");
        return result;
    }
}