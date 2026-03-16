package com.qz.sns.sv.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qz.sns.model.entity.SurveyUserStatistic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 用户问卷参与统计表 Mapper 接口
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
@Mapper
@Repository
public interface SurveyUserStatisticMapper extends BaseMapper<SurveyUserStatistic> {

    /**
     * 根据用户ID查询统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    SurveyUserStatistic selectByUserId(@Param("userId") Long userId);

    /**
     * 增加用户参与问卷数
     *
     * @param userId 用户ID
     * @param points 获得的积分
     * @return 影响行数
     */
    int incrementUserSurveyCount(@Param("userId") Long userId, @Param("points") Integer points);
}



