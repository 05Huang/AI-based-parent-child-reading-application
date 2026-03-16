package com.qz.sns.sv.service.impl;


import com.qz.sns.common.constant.UserBehaviorConstant;
import com.qz.sns.model.dto.UserBehaviorDTO;
import com.qz.sns.model.entity.UserBehavior;
import com.qz.sns.model.vo.BrowsingStatsVO;
import com.qz.sns.model.vo.CollectionStatsVO;
import com.qz.sns.model.vo.HistoryStatsVO;
import com.qz.sns.model.vo.ReadingRankingVO;
import com.qz.sns.model.vo.WeeklyReportVO;
import com.qz.sns.sv.mapper.StatisticsMapper;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    @Autowired
    private StatisticsMapper statisticsMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public UserBehavior recordUserBehavior(UserBehaviorDTO dto) {
        UserBehavior behavior = new UserBehavior();
        BeanUtils.copyProperties(dto, behavior);
        behavior.setCreatedTime(new Date());
        
        // 插入记录
        userBehaviorMapper.insert(behavior);
        log.info("记录用户行为成功：userId={}, contentId={}, behaviorType={}", 
                dto.getUserId(), dto.getContentId(), dto.getBehaviorType());

        try {
            clearUserStatsCache(dto.getUserId());
        } catch (Exception e) {
            log.warn("清除用户统计缓存失败：userId={}", dto.getUserId(), e);
        }
        
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

    @Override
    public WeeklyReportVO getWeeklyReport(Long userId, String year, String month) {
        // 如果指定了年月，不使用缓存，直接查询指定时间的数据
        if (year != null && month != null) {
            return generateWeeklyReportByTime(userId, year, month);
        }
        
        // 如果没有指定时间，使用原有逻辑
        return getWeeklyReport(userId);
    }

    @Override
    public Integer getWeeklyReadingTargetMinutes(Long userId) {
        Integer v = statisticsMapper.getWeeklyReadTargetMinutes(userId);
        if (v == null || v <= 0) return 30;
        return v;
    }

    @Override
    public void setWeeklyReadingTargetMinutes(Long userId, Integer targetMinutes) {
        if (targetMinutes == null || targetMinutes <= 0) {
            throw new IllegalArgumentException("目标阅读时间必须大于0");
        }
        statisticsMapper.upsertWeeklyReadTargetMinutes(userId, targetMinutes);
    }

    private WeeklyReportVO generateWeeklyReport(Long userId) {
        // 修改为自然周（周一到周日），而不是滚动7天
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

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

    private WeeklyReportVO generateWeeklyReportByTime(Long userId, String year, String month) {
        try {
            // 解析年月参数
            int yearInt = Integer.parseInt(year);
            int monthInt = Integer.parseInt(month);
            
            // 计算指定月份的开始和结束时间
            LocalDateTime monthStart = LocalDateTime.of(yearInt, monthInt, 1, 0, 0, 0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
            
            // 如果查询的是未来月份，返回空数据
            if (monthStart.isAfter(LocalDateTime.now())) {
                return createEmptyReport();
            }
            
            WeeklyReportVO report = new WeeklyReportVO();

            // 浏览总时长
            Long totalDuration = userBehaviorMapper.getMonthlyReadDuration(userId, monthStart, monthEnd);
            report.setTotalReadDuration(totalDuration);

            // 浏览文章数
            Long articleCount = userBehaviorMapper.getWeeklyViewCount(userId, monthStart, monthEnd);
            report.setTotalArticleCount(articleCount);

            // 互动次数（按月份查询）
            Long interactionCount = userBehaviorMapper.getInteractionCountByTime(userId, monthStart, monthEnd);
            report.setInteractionCount(interactionCount);

            // 活跃度计算：基于阅读时长、文章数、互动次数的综合评分
            BigDecimal activityScore = calculateActivityScore(totalDuration, articleCount, interactionCount);
            report.setActivityScore(activityScore);

            // 每日平均阅读时间
            BigDecimal dailyAvg = BigDecimal.ZERO;
            if (totalDuration > 0) {
                int daysInMonth = monthStart.toLocalDate().lengthOfMonth();
                dailyAvg = new BigDecimal(totalDuration).divide(new BigDecimal(daysInMonth), 2, RoundingMode.HALF_UP);
            }
            report.setDailyAvgReadTime(dailyAvg);

            // 最感兴趣的阅读分类
            Map<String, Object> categoryInfo = userBehaviorMapper.getFavoriteCategory(userId, monthStart, monthEnd);
            String favoriteCategory = categoryInfo != null ? (String) categoryInfo.get("name") : "暂无数据";
            report.setFavoriteCategory(favoriteCategory);

            return report;
        } catch (Exception e) {
            log.error("生成指定时间的周报失败：{}", e.getMessage(), e);
            return createEmptyReport();
        }
    }

    private WeeklyReportVO createEmptyReport() {
        WeeklyReportVO report = new WeeklyReportVO();
        report.setTotalReadDuration(0L);
        report.setTotalArticleCount(0L);
        report.setInteractionCount(0L);
        report.setActivityScore(BigDecimal.ZERO);
        report.setDailyAvgReadTime(BigDecimal.ZERO);
        report.setFavoriteCategory("暂无数据");
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

    @Override
    public Integer getConsecutiveReadingDays(Long userId) {
        try {
            log.info("开始计算用户{}的连续阅读天数", userId);
            
            // 获取最近60天的阅读日期
            LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
            List<String> readingDates = userBehaviorMapper.getReadingDates(userId, sixtyDaysAgo);
            
            if (readingDates == null || readingDates.isEmpty()) {
                log.info("用户{}没有阅读记录，连续阅读天数为0", userId);
                return 0;
            }
            
            // 将日期列表转换为Set以便快速查找
            java.util.Set<String> readingDateSet = new java.util.HashSet<>(readingDates);
            
            // 计算连续天数
            LocalDate today = LocalDate.now();
            int consecutiveDays = 0;
            boolean foundBreak = false;
            
            // 从今天开始往前检查
            for (int i = 0; i < 60; i++) {
                LocalDate checkDate = today.minusDays(i);
                String dateStr = checkDate.toString();
                
                // 检查这一天是否有阅读记录
                boolean hasReading = readingDateSet.contains(dateStr);
                
                if (hasReading) {
                    consecutiveDays++;
                } else {
                    // 如果今天没有阅读，不算连续；如果今天有阅读，但昨天没有，则中断
                    if (i == 0) {
                        // 今天没有阅读，从昨天开始计算
                        continue;
                    } else {
                        // 遇到中断，停止计算
                        foundBreak = true;
                        break;
                    }
                }
            }
            
            log.info("用户{}的连续阅读天数：{}", userId, consecutiveDays);
            return consecutiveDays;
            
        } catch (Exception e) {
            log.error("计算用户{}连续阅读天数失败", userId, e);
            return 0;
        }
    }

    @Override
    public List<ReadingRankingVO> getWeeklyReadingRanking(Integer limit) {
        try {
            // 默认限制为20
            if (limit == null || limit <= 0) {
                limit = 20;
            }
            
            // 计算本周的开始时间（周一00:00:00）和结束时间（下周一00:00:00）
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime weekEnd = weekStart.plusWeeks(1);
            
            log.info("获取周阅读排行榜（全网用户），时间范围：{} 到 {}，限制：{}", weekStart, weekEnd, limit);
            
            // 调用Mapper查询数据
            List<Map<String, Object>> rankingData = userBehaviorMapper.getWeeklyReadingRanking(
                    weekStart, weekEnd, limit);
            
            log.info("数据库查询返回{}条原始记录", rankingData.size());
            if (rankingData.isEmpty()) {
                log.warn("未查询到任何阅读行为记录，可能原因：1) 本周没有阅读行为 2) 时间范围不正确 3) 没有孩子用户有阅读行为");
            }
            
            // 转换为VO对象
            List<ReadingRankingVO> result = new ArrayList<>();
            for (int i = 0; i < rankingData.size(); i++) {
                Map<String, Object> item = rankingData.get(i);
                ReadingRankingVO vo = new ReadingRankingVO();
                
                vo.setRank(i + 1);
                
                // 处理用户ID
                Object userIdObj = item.get("userId");
                if (userIdObj instanceof Number) {
                    vo.setUserId(((Number) userIdObj).longValue());
                } else {
                    log.warn("用户ID格式异常：{}", userIdObj);
                    continue; // 跳过无效记录
                }
                
                vo.setNickname((String) item.get("nickname"));
                vo.setUsername((String) item.get("username"));
                vo.setAvatar((String) item.get("avatar"));
                
                // 处理阅读时长
                Object readDurationObj = item.get("readDuration");
                if (readDurationObj instanceof Number) {
                    vo.setReadDuration(((Number) readDurationObj).longValue());
                } else {
                    vo.setReadDuration(0L);
                }
                
                // 趋势信息暂时设为持平（后续可以根据历史数据计算）
                vo.setTrend("equal");
                vo.setTrendNumber(0);
                
                result.add(vo);
                log.debug("处理排行榜记录：用户ID={}, 昵称={}, 阅读时长={}秒", 
                        vo.getUserId(), vo.getNickname(), vo.getReadDuration());
            }
            
            log.info("周阅读排行榜查询成功，返回{}条记录", result.size());
            return result;
        } catch (Exception e) {
            log.error("获取周阅读排行榜失败", e);
            throw new RuntimeException("获取周阅读排行榜失败：" + e.getMessage(), e);
        }
    }
}
