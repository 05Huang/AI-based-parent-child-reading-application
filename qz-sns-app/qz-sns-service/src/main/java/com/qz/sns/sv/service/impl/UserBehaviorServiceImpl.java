package com.qz.sns.sv.service.impl;


import com.qz.sns.common.constant.UserBehaviorConstant;
import com.qz.sns.model.dto.UserBehaviorDTO;
import com.qz.sns.model.entity.UserBehavior;
import com.qz.sns.model.vo.BrowsingStatsVO;
import com.qz.sns.model.vo.CollectionStatsVO;
import com.qz.sns.model.vo.HistoryStatsVO;
import com.qz.sns.model.vo.WeeklyReportVO;
import com.qz.sns.sv.mapper.UserBehaviorMapper;
import com.qz.sns.sv.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.qz.sns.common.constant.UserBehaviorConstant.HISTORY_STATS_KEY;
import static com.qz.sns.common.constant.UserBehaviorConstant.WEEKLY_REPORT_KEY;

/**
 * 用户行为服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserBehaviorServiceImpl implements UserBehaviorService {
    
    @Autowired
    private UserBehaviorMapper userBehaviorMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public UserBehavior recordUserBehavior(UserBehaviorDTO dto) {
        UserBehavior behavior = new UserBehavior();
        BeanUtils.copyProperties(dto, behavior);
        
        // 插入记录
        userBehaviorMapper.insert(behavior);
        log.info("记录用户行为成功：userId={}, contentId={}, behaviorType={}", 
                dto.getUserId(), dto.getContentId(), dto.getBehaviorType());
        
        return behavior;
    }

    @Override
    public BrowsingStatsVO getBrowsingStats(Long userId) {
        String cacheKey = UserBehaviorConstant.BROWSING_STATS_KEY + userId;
        BrowsingStatsVO cached = (BrowsingStatsVO) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return cached;
        }

        BrowsingStatsVO stats = new BrowsingStatsVO();

        // 阅读总时长
        stats.setTotalReadDuration(userBehaviorMapper.getTotalReadDuration(userId));

        // 阅读总数
        stats.setTotalReadCount(userBehaviorMapper.getTotalReadCount(userId));

        // 本周浏览数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();
        stats.setWeeklyViewCount(userBehaviorMapper.getWeeklyViewCount(userId, weekStart, now));

        // 互动频率 = 互动行为数 / 浏览数
        Long interactionCount = userBehaviorMapper.getInteractionCount(userId);
        Long viewCount = userBehaviorMapper.getViewCount(userId);
        BigDecimal interactionRate = BigDecimal.ZERO;
        if (viewCount > 0) {
            interactionRate = new BigDecimal(interactionCount)
                    .divide(new BigDecimal(viewCount), 4, RoundingMode.HALF_UP);
        }
        stats.setInteractionRate(interactionRate);

        // 缓存1天
        redisTemplate.opsForValue().set(cacheKey, stats, UserBehaviorConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return stats;
    }

    @Override
    public CollectionStatsVO getCollectionStats(Long userId) {
        String cacheKey = UserBehaviorConstant.COLLECTION_STATS_KEY + userId;
        CollectionStatsVO cached = (CollectionStatsVO) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return cached;
        }

        CollectionStatsVO stats = new CollectionStatsVO();

        // 收藏总数
        stats.setTotalCollections(userBehaviorMapper.getTotalCollections(userId));

        // 本月收藏
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        stats.setMonthlyCollections(userBehaviorMapper.getMonthlyCollections(userId, monthStart, now));

        // 收藏分享数
        stats.setCollectionShares(userBehaviorMapper.getCollectionShares(userId));

        // 互动数(对收藏内容的互动)
        stats.setInteractionCount(userBehaviorMapper.getInteractionCount(userId));

        // 缓存1天
        redisTemplate.opsForValue().set(cacheKey, stats, UserBehaviorConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return stats;
    }

    @Override
    public HistoryStatsVO getHistoryStats(Long userId) {
        String cacheKey = HISTORY_STATS_KEY + userId;
        HistoryStatsVO cached = (HistoryStatsVO) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return cached;
        }

        HistoryStatsVO stats = new HistoryStatsVO();

        // 本月浏览时长
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        stats.setMonthlyReadDuration(userBehaviorMapper.getMonthlyReadDuration(userId, monthStart, now));

        // 浏览文章总数
        stats.setTotalArticleCount(userBehaviorMapper.getTotalReadCount(userId));

        // 互动文章数
        stats.setInteractedArticleCount(userBehaviorMapper.getInteractedArticleCount(userId));

        // 分享次数
        stats.setShareCount(userBehaviorMapper.getShareCount(userId));

        // 缓存1天
        redisTemplate.opsForValue().set(cacheKey, stats, UserBehaviorConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        return stats;
    }

    @Override
    public WeeklyReportVO getWeeklyReport(Long userId) {
        String cacheKey = WEEKLY_REPORT_KEY + userId;
        WeeklyReportVO cached = (WeeklyReportVO) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return cached;
        }

        return generateWeeklyReport(userId);
    }

    private WeeklyReportVO generateWeeklyReport(Long userId) {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();

        WeeklyReportVO report = new WeeklyReportVO();

        // 浏览总时长
        Long totalDuration = userBehaviorMapper.getMonthlyReadDuration(userId, weekStart, now);
        report.setTotalReadDuration(totalDuration);

        // 浏览文章数
        Long articleCount = userBehaviorMapper.getWeeklyViewCount(userId, weekStart, now);
        report.setTotalArticleCount(articleCount);

        // 互动次数
        Long interactionCount = userBehaviorMapper.getInteractionCount(userId);
        report.setInteractionCount(interactionCount);

        // 活跃度计算：基于阅读时长、文章数、互动次数的综合评分
        BigDecimal activityScore = calculateActivityScore(totalDuration, articleCount, interactionCount);
        report.setActivityScore(activityScore);

        // 每日平均阅读时间
        BigDecimal dailyAvg = BigDecimal.ZERO;
        if (totalDuration > 0) {
            dailyAvg = new BigDecimal(totalDuration).divide(new BigDecimal(7), 2, RoundingMode.HALF_UP);
        }
        report.setDailyAvgReadTime(dailyAvg);

        // 最感兴趣的阅读分类
        Map<String, Object> categoryInfo = userBehaviorMapper.getFavoriteCategory(userId, weekStart, now);
        System.out.println(categoryInfo);
        String favoriteCategory = categoryInfo != null ? (String) categoryInfo.get("name") : "暂无数据";
        report.setFavoriteCategory(favoriteCategory);

        return report;
    }

    private BigDecimal calculateActivityScore(Long duration, Long articleCount, Long interactionCount) {
        // 活跃度算法：时长权重30%，文章数权重40%，互动权重30%
        BigDecimal durationScore = new BigDecimal(Math.min(duration / 360.0, 20)); // 最高10分，每小时1分
        BigDecimal articleScore = new BigDecimal(Math.min(articleCount, 20)); // 最高20分，每篇文章1分
        BigDecimal interactionScore = new BigDecimal(Math.min(interactionCount * 2, 40)); // 最高20分，每次互动2分

        return durationScore.multiply(new BigDecimal("0.3"))
                .add(articleScore.multiply(new BigDecimal("0.4")))
                .add(interactionScore.multiply(new BigDecimal("0.3")))
                .setScale(2, RoundingMode.HALF_UP);// 保留两位小数
    }



    @Override
    public void clearUserStatsCache(Long userId) {
        try {
            redisTemplate.delete(UserBehaviorConstant.BROWSING_STATS_KEY + userId);
            redisTemplate.delete(UserBehaviorConstant.COLLECTION_STATS_KEY + userId);
            redisTemplate.delete(UserBehaviorConstant.HISTORY_STATS_KEY + userId);
            redisTemplate.delete(UserBehaviorConstant.WEEKLY_REPORT_KEY + userId);
            log.info("用户{}的统计缓存清除成功", userId);
        } catch (Exception e) {
            log.error("清除用户{}统计缓存失败", userId, e);
            throw new RuntimeException("清除缓存失败", e);
        }
    }
}