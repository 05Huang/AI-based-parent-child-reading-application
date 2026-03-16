package com.qz.sns.web.controller;

import com.qz.sns.common.enums.ResultCode;

import com.qz.sns.model.dto.UserBehaviorDTO;
import com.qz.sns.model.entity.UserBehavior;
import com.qz.sns.model.vo.BrowsingStatsVO;
import com.qz.sns.model.vo.CollectionStatsVO;
import com.qz.sns.model.vo.HistoryStatsVO;
import com.qz.sns.model.vo.ReadingRankingVO;
import com.qz.sns.model.vo.WeeklyReportVO;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.UserBehaviorService;
import com.qz.sns.sv.service.impl.UserBehaviorServiceImpl;
import com.qz.sns.sv.session.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 用户行为控制器
 * 处理用户行为记录相关请求
 */
@RestController
@RequestMapping("/api/user/behavior")
@Slf4j
@RequiredArgsConstructor
public class UserBehaviorController {

    @Autowired
    private UserBehaviorService userBehaviorService;
    private final UserBehaviorServiceImpl userStatsService;

    /**
     * 记录用户行为
     * 当用户退出文章阅读时，记录阅读行为信息
     * 
     * @param dto 用户行为数据传输对象
     * @return 标准响应结果
     */
    @PostMapping("/record")
    public Result<Long> recordUserBehavior(@RequestBody UserBehaviorDTO dto) {
        try {
            // 参数验证
            if (dto.getUserId() == null || dto.getUserId() <= 0) {
                return ResultUtils.error(ResultCode.PARAM_ERROR, "用户ID不能为空");
            }
            
            if (dto.getContentId() == null || dto.getContentId() <= 0) {
                return ResultUtils.error(ResultCode.PARAM_ERROR, "内容ID不能为空");
            }
            
            if (!StringUtils.hasText(dto.getBehaviorType())) {
                return ResultUtils.error(ResultCode.PARAM_ERROR, "行为类型不能为空");
            }
            
            // 验证行为类型是否合法
            List<String> validBehaviorTypes = Arrays.asList("view", "like", "comment", "share", "collect");
            if (!validBehaviorTypes.contains(dto.getBehaviorType())) {
                return ResultUtils.error(
                    ResultCode.PARAM_ERROR, 
                    "行为类型无效，有效值为：view, like, comment, share, collect"
                );
            }
            
            // 验证进度百分比范围
            if (dto.getProgress() != null && 
                (dto.getProgress().doubleValue() < 0 || dto.getProgress().doubleValue() > 100)) {
                return ResultUtils.error(ResultCode.PARAM_ERROR, "进度百分比必须在0-100之间");
            }
            
            // 调用服务层添加记录
            UserBehavior behavior = userBehaviorService.recordUserBehavior(dto);
            
            // 返回成功结果和记录ID
            return ResultUtils.success(behavior.getId(), "行为记录添加成功");
        } catch (Exception e) {
            log.error("添加用户行为记录失败", e);
            return ResultUtils.error(ResultCode.PROGRAM_ERROR, "系统错误，请稍后再试");
        }

    }

    @Operation(summary = "获取浏览记录统计")
    @GetMapping("/browsing/{userId}")
    public Result<BrowsingStatsVO> getBrowsingStats(@PathVariable Long userId) {
        try {
            BrowsingStatsVO stats = userStatsService.getBrowsingStats(userId);
            return ResultUtils.success(stats);
        } catch (Exception e) {
            return ResultUtils.error(500, "获取浏览统计数据失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取收藏统计")
    @GetMapping("/collection/{userId}")
    public Result<CollectionStatsVO> getCollectionStats(@PathVariable Long userId) {
        try {
            CollectionStatsVO stats = userStatsService.getCollectionStats(userId);
            return ResultUtils.success(stats);
        } catch (Exception e) {
            return ResultUtils.error(500, "获取收藏统计数据失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取历史统计")
    @GetMapping("/history/{userId}")
    public Result<HistoryStatsVO> getHistoryStats(@PathVariable Long userId) {
        try {
            HistoryStatsVO stats = userStatsService.getHistoryStats(userId);
            return ResultUtils.success(stats);
        } catch (Exception e) {
            return ResultUtils.error(500, "获取历史统计数据失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取周报")
    @GetMapping("/weekly-report/{userId}")
    public Result<WeeklyReportVO> getWeeklyReport(@PathVariable Long userId,
                                                  @RequestParam(required = false) String year,
                                                  @RequestParam(required = false) String month) {
        try {
            WeeklyReportVO report = userStatsService.getWeeklyReport(userId, year, month);
            return ResultUtils.success(report);
        } catch (Exception e) {
            return ResultUtils.error(500, "获取周报数据失败：" + e.getMessage());
        }
    }

    @Operation(summary = "手动刷新用户统计缓存")
    @PostMapping("/refresh-cache/{userId}")
    public Result<String> refreshUserStatsCache(@PathVariable Long userId) {
        try {
            // 清除该用户的所有统计缓存
            userStatsService.clearUserStatsCache(userId);
            return ResultUtils.success("缓存刷新成功");
        } catch (Exception e) {
            return ResultUtils.error(500, "缓存刷新失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取周阅读排行榜")
    @GetMapping("/weekly-ranking")
    public Result<List<ReadingRankingVO>> getWeeklyReadingRanking(
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        try {
            List<ReadingRankingVO> ranking = userBehaviorService.getWeeklyReadingRanking(limit);
            return ResultUtils.success(ranking);
        } catch (Exception e) {
            log.error("获取周阅读排行榜失败", e);
            return ResultUtils.error(500, "获取周阅读排行榜失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取连续阅读天数")
    @GetMapping("/consecutive-days/{userId}")
    public Result<Integer> getConsecutiveReadingDays(@PathVariable Long userId) {
        try {
            Integer days = userBehaviorService.getConsecutiveReadingDays(userId);
            return ResultUtils.success(days);
        } catch (Exception e) {
            log.error("获取连续阅读天数失败，用户ID：{}", userId, e);
            return ResultUtils.error(500, "获取连续阅读天数失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取每周目标阅读时间（分钟）")
    @GetMapping("/reading-target/{userId}")
    public Result<Integer> getWeeklyReadingTargetMinutes(@PathVariable Long userId) {
        try {
            Integer minutes = userBehaviorService.getWeeklyReadingTargetMinutes(userId);
            return ResultUtils.success(minutes);
        } catch (Exception e) {
            log.error("获取目标阅读时间失败，用户ID：{}", userId, e);
            return ResultUtils.error(500, "获取目标阅读时间失败：" + e.getMessage());
        }
    }

    @Operation(summary = "设置每周目标阅读时间（分钟）")
    @PostMapping("/reading-target")
    public Result<String> setWeeklyReadingTargetMinutes(@RequestParam Long userId, @RequestParam Integer targetMinutes) {
        try {
            Long sessionUserId = SessionContext.getSession() != null ? SessionContext.getSession().getUserId() : null;
            if (sessionUserId == null) {
                return ResultUtils.error(ResultCode.NO_LOGIN.getCode(), "未登录");
            }
            if (!sessionUserId.equals(userId)) {
                return ResultUtils.error(ResultCode.FORBIDDEN.getCode(), "无权限修改他人目标");
            }
            userBehaviorService.setWeeklyReadingTargetMinutes(userId, targetMinutes);
            return ResultUtils.success("ok");
        } catch (IllegalArgumentException e) {
            return ResultUtils.error(ResultCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("设置目标阅读时间失败，userId={} targetMinutes={}", userId, targetMinutes, e);
            return ResultUtils.error(500, "设置目标阅读时间失败：" + e.getMessage());
        }
    }
}
