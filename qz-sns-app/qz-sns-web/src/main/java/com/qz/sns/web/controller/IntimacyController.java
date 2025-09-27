package com.qz.sns.web.controller;


import com.qz.sns.model.vo.IntimacyRankingVO;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
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
    public Result<Map<String, Object>> getGlobalRanking() {
        log.info("开始获取全网亲密度排行");
        List<IntimacyRankingVO> ranking = intimacyService.getGlobalRanking();
        Map<String, Object> result = new HashMap<>();
        result.put("ranking", ranking);
        log.info("全网亲密度排行获取成功，共{}条记录", ranking.size());
        return ResultUtils.success(result);
    }

    /**
     * 获取用户家庭亲密度排行
     */
    @GetMapping("/user-ranking/{userId}")
    public Result<Map<String, Object>> getUserRanking(@PathVariable Long userId) {
        log.info("开始获取用户{}的家庭亲密度排行", userId);
        
        // 获取用户家庭内排行
        List<IntimacyRankingVO> ranking = intimacyService.getUserRanking(userId);
        
        // 获取用户在全网的排名和亲密度
        Map<String, Object> globalInfo = intimacyService.getUserGlobalInfo(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("ranking", ranking);
        result.put("globalRank", globalInfo.get("globalRank"));
        result.put("intimacyPercentage", globalInfo.get("intimacyPercentage"));
        
        log.info("用户{}的亲密度信息获取成功：家庭排行{}条，全网排名{}，亲密度{}%", 
                userId, ranking.size(), globalInfo.get("globalRank"), globalInfo.get("intimacyPercentage"));
        
        return ResultUtils.success(result);
    }

    /**
     * 手动触发亲密度计算（用于测试）
     */
    @PostMapping("/calculate")
    public Result<Map<String, Object>> calculateIntimacy() {
        log.info("手动触发亲密度计算");
        intimacyService.calculateAndSaveAllIntimacyScores();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "亲密度计算已触发");
        log.info("亲密度计算触发成功");
        return ResultUtils.success(result, "亲密度计算已触发");
    }
    
    /**
     * 清除缓存并重新计算亲密度（用于测试）
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshIntimacy() {
        log.info("清除亲密度缓存并重新计算");
        intimacyService.clearCacheAndRecalculate();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "亲密度缓存已清除并重新计算");
        log.info("亲密度缓存清除和重新计算完成");
        return ResultUtils.success(result, "亲密度缓存已清除并重新计算");
    }
}