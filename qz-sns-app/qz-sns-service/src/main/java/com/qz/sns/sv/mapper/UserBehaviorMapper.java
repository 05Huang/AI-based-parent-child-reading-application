package com.qz.sns.sv.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qz.sns.model.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户行为数据访问层
 */
@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    @Select("SELECT COALESCE(SUM(duration), 0) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'view'")
    Long getTotalReadDuration(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT content_id) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'view'")
    Long getTotalReadCount(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT content_id) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'view' " +
            "AND created_time >= #{startTime} AND created_time < #{endTime}")
    Long getWeeklyViewCount(@Param("userId") Long userId,
                            @Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(*) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type IN ('like', 'comment', 'share')")
    Long getInteractionCount(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type IN ('like', 'comment', 'share') " +
            "AND created_time >= #{startTime} AND created_time < #{endTime}")
    Long getInteractionCountByTime(@Param("userId") Long userId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(DISTINCT content_id) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'view'")
    Long getViewCount(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT content_id) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'collect'")
    Long getTotalCollections(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT content_id) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'collect' " +
            "AND created_time >= #{startTime} AND created_time < #{endTime}")
    Long getMonthlyCollections(@Param("userId") Long userId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(*) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'share' " +
            "AND content_id IN (SELECT content_id FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'collect')")
    Long getCollectionShares(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(duration), 0) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'view' " +
            "AND created_time >= #{startTime} AND created_time < #{endTime}")
    Long getMonthlyReadDuration(@Param("userId") Long userId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(DISTINCT content_id) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type IN ('like', 'comment', 'share')")
    Long getInteractedArticleCount(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'share'")
    Long getShareCount(@Param("userId") Long userId);

    @Select("SELECT cc.name, COUNT(DISTINCT ub.content_id) as count " +
            "FROM user_behavior ub " +
            "JOIN content c ON ub.content_id = c.id " +
            "JOIN content_category cc ON c.category_id = cc.id " +
            "WHERE ub.user_id = #{userId} AND ub.behavior_type = 'view' " +
            "AND ub.created_time >= #{startTime} AND ub.created_time < #{endTime} " +
            "GROUP BY cc.id, cc.name " +
            "ORDER BY count DESC LIMIT 1")
    Map<String, Object> getFavoriteCategory(@Param("userId") Long userId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
}