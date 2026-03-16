package com.qz.sns.web.controller;

import com.qz.sns.common.enums.ResultCode;
import com.qz.sns.model.dto.ContentRequest;
import com.qz.sns.model.dto.RecommendationRequestDTO;
import com.qz.sns.sv.mapper.ContentMapper;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendation")
@Slf4j
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ContentMapper contentMapper;

    /**
     * 获取推荐内容
     */
    @PostMapping("/get")
    public Result<List<ContentRequest>> getRecommendations(@RequestBody RecommendationRequestDTO request) {
        try {
            // 参数验证
            if (request.getUserId() == null) {
                return ResultUtils.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
            }

            // 获取推荐内容
            List<ContentRequest> recommendations = recommendationService.getRecommendedContents(
                    request.getUserId(),
                    request.getSize(),
                    request.getType(),
                    request.getShuffle()
            );

            if (recommendations.isEmpty()) {
                return ResultUtils.success(recommendations, "暂无推荐内容");
            }

            return ResultUtils.success(recommendations);

        } catch (Exception e) {
            log.error("Error getting recommendations for user: {}", request.getUserId(), e);
            return ResultUtils.error(ResultCode.SERVER_ERROR, "获取推荐内容失败");
        }
    }

    /**
     * 获取热门内容（用于冷启动）
     */
    @GetMapping("/hot")
    public Result<List<ContentRequest>> getHotContents(
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "shuffle", required = false) Boolean shuffle) {
        try {
            // 调用 Python 接口获取热门内容
            List<ContentRequest> hotContents = recommendationService.getHotContents(size, userId, type, shuffle);

            return ResultUtils.success(hotContents);

        } catch (Exception e) {
            log.error("Error getting hot contents", e);
            return ResultUtils.error(ResultCode.SERVER_ERROR, "获取热门内容失败");
        }
    }

    /**
     * 更新用户行为（通知 Python 端清除缓存）
     */
    @PostMapping("/updateBehavior")
    public Result<Void> updateUserBehavior(@RequestParam Long userId) {
        try {
            if (userId == null) {
                return ResultUtils.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
            }

            recommendationService.updateUserBehavior(userId);

            return ResultUtils.success("用户行为更新成功");

        } catch (Exception e) {
            log.error("Error updating user behavior for user: {}", userId, e);
            return ResultUtils.error(ResultCode.SERVER_ERROR, "更新用户行为失败");
        }
    }
    /**
     * 系统健康检查
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            // 检查数据库连接
            boolean dbStatus = checkDatabaseConnection();
            healthInfo.put("database", dbStatus ? "UP" : "DOWN");

            // 检查 Python 服务
            boolean pythonStatus = checkPythonService();
            healthInfo.put("pythonService", pythonStatus ? "UP" : "DOWN");

            // 系统信息
            healthInfo.put("timestamp", LocalDateTime.now());
            healthInfo.put("service", "recommendation-service");
            healthInfo.put("version", "1.0.0");

            // 总体状态
            boolean overallStatus = dbStatus && pythonStatus;
            healthInfo.put("status", overallStatus ? "HEALTHY" : "UNHEALTHY");

            if (overallStatus) {
                return ResultUtils.success(healthInfo, "系统运行正常");
            } else {
                return ResultUtils.error(ResultCode.SERVER_ERROR, "系统存在异常");
            }

        } catch (Exception e) {
            log.error("Health check failed", e);
            healthInfo.put("status", "ERROR");
            healthInfo.put("error", e.getMessage());
            return ResultUtils.error(ResultCode.SERVER_ERROR, "健康检查失败");
        }
    }

    private boolean checkDatabaseConnection() {
        try {
            // 执行一个简单的查询来检查数据库连接
            contentMapper.selectById(1L);
            return true;
        } catch (Exception e) {
            log.error("Database connection check failed", e);
            return false;
        }
    }

    private boolean checkPythonService() {
        try {
            URL url = new URL("http://localhost:5000/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            log.error("Python service check failed", e);
            return false;
        }
    }
}
