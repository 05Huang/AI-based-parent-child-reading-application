package com.qz.sns.sv.service;


import com.qz.sns.model.dto.UserBehaviorDTO;
import com.qz.sns.model.entity.UserBehavior;
import com.qz.sns.model.vo.BrowsingStatsVO;
import com.qz.sns.model.vo.CollectionStatsVO;
import com.qz.sns.model.vo.HistoryStatsVO;
import com.qz.sns.model.vo.WeeklyReportVO;

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


    void clearUserStatsCache(Long userId);
}