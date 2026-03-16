package com.qz.sns.sv.service;


import com.qz.sns.model.dto.UserBehaviorDTO;
import com.qz.sns.model.entity.UserBehavior;
import com.qz.sns.model.vo.BrowsingStatsVO;
import com.qz.sns.model.vo.CollectionStatsVO;
import com.qz.sns.model.vo.HistoryStatsVO;
import com.qz.sns.model.vo.ReadingRankingVO;
import com.qz.sns.model.vo.WeeklyReportVO;

import java.util.List;

/**
 * 用户行为服务接口
 */
public interface UserBehaviorService {
    
    /**
     * 记录用户行为
     * @param dto 用户行为请求DTO
     * @return 记录的行为实体
     */
    UserBehavior recordUserBehavior(UserBehaviorDTO dto);


    BrowsingStatsVO getBrowsingStats(Long userId);

    CollectionStatsVO getCollectionStats(Long userId);

    HistoryStatsVO getHistoryStats(Long userId);

    WeeklyReportVO getWeeklyReport(Long userId);

    WeeklyReportVO getWeeklyReport(Long userId, String year, String month);

    void clearUserStatsCache(Long userId);

    /**
     * 获取周阅读排行榜
     * @param limit 返回数量限制，默认20
     * @return 阅读排行榜列表
     */
    List<ReadingRankingVO> getWeeklyReadingRanking(Integer limit);

    /**
     * 获取连续阅读天数
     * @param userId 用户ID
     * @return 连续阅读天数
     */
    Integer getConsecutiveReadingDays(Long userId);

    Integer getWeeklyReadingTargetMinutes(Long userId);

    void setWeeklyReadingTargetMinutes(Long userId, Integer targetMinutes);
}
