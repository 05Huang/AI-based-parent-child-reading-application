package com.qz.sns.sv.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qz.sns.model.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * 查询两个用户共同阅读的内容数量
     */
    @Select("SELECT COUNT(DISTINCT ub1.content_id) " +
            "FROM user_behavior ub1 " +
            "INNER JOIN user_behavior ub2 ON ub1.content_id = ub2.content_id " +
            "WHERE ub1.user_id = #{userId} AND ub2.user_id = #{relativeId} " +
            "AND ub1.behavior_type = 'view' AND ub2.behavior_type = 'view' " +
            "AND ub1.created_time >= #{startTime} AND ub2.created_time >= #{startTime}")
    Long countSharedReading(@Param("userId") Long userId,
                            @Param("relativeId") Long relativeId,
                            @Param("startTime") LocalDateTime startTime);

    /**
     * 查询两个用户之间的互动行为数（点赞、评论、分享）
     */
    @Select("SELECT COUNT(*) " +
            "FROM user_behavior ub1 " +
            "INNER JOIN user_behavior ub2 ON ub1.content_id = ub2.content_id " +
            "WHERE ((ub1.user_id = #{userId} AND ub2.user_id = #{relativeId}) " +
            "OR (ub1.user_id = #{relativeId} AND ub2.user_id = #{userId})) " +
            "AND ub1.behavior_type IN ('like', 'comment', 'share') " +
            "AND ub1.created_time >= #{startTime} " +
            "AND ub2.created_time >= #{startTime}")
    Long countInteractions(@Param("userId") Long userId,
                           @Param("relativeId") Long relativeId,
                           @Param("startTime") LocalDateTime startTime);

    /**
     * 查询两个用户在同一天阅读相同内容的次数
     */
    @Select("SELECT COUNT(DISTINCT ub1.content_id) " +
            "FROM user_behavior ub1 " +
            "INNER JOIN user_behavior ub2 ON ub1.content_id = ub2.content_id " +
            "WHERE ub1.user_id = #{userId} AND ub2.user_id = #{relativeId} " +
            "AND ub1.behavior_type = 'view' AND ub2.behavior_type = 'view' " +
            "AND DATE(ub1.created_time) = DATE(ub2.created_time) " +
            "AND ub1.created_time >= #{startTime} AND ub2.created_time >= #{startTime}")
    Long countSameDayReading(@Param("userId") Long userId,
                             @Param("relativeId") Long relativeId,
                             @Param("startTime") LocalDateTime startTime);

    /**
     * 获取周阅读排行榜
     * 查询本周（从周一开始）所有孩子类型用户（role=2）的阅读时长总和，按时长降序排列
     * 包括所有有阅读行为的孩子用户，即使时长为0也显示
     */
    @Select("SELECT " +
            "    u.id as userId, " +
            "    u.nickname, " +
            "    u.username, " +
            "    u.avatar, " +
            "    COALESCE(SUM(CASE WHEN ub.duration IS NOT NULL THEN ub.duration ELSE 0 END), 0) as readDuration " +
            "FROM user u " +
            "INNER JOIN user_behavior ub ON u.id = ub.user_id " +
            "    AND ub.behavior_type = 'view' " +
            "    AND ub.created_time >= #{startTime} " +
            "    AND ub.created_time < #{endTime} " +
            "WHERE u.status = 1 " +
            "GROUP BY u.id, u.nickname, u.username, u.avatar " +
            "ORDER BY readDuration DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getWeeklyReadingRanking(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("limit") Integer limit);

    /**
     * 统计单个用户本周的总互动次数（点赞、评论、分享）
     */
    @Select("SELECT COUNT(*) FROM user_behavior " +
            "WHERE user_id = #{userId} " +
            "AND behavior_type IN ('like', 'comment', 'share') " +
            "AND created_time >= #{startTime} AND created_time < #{endTime}")
    Long countUserInteractionsInPeriod(@Param("userId") Long userId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户有阅读行为的日期列表（最近N天）
     * 用于计算连续阅读天数
     */
    @Select("SELECT DISTINCT DATE(created_time) as read_date " +
            "FROM user_behavior " +
            "WHERE user_id = #{userId} " +
            "AND behavior_type = 'view' " +
            "AND created_time >= #{startTime} " +
            "ORDER BY read_date DESC")
    List<String> getReadingDates(@Param("userId") Long userId,
                                 @Param("startTime") LocalDateTime startTime);
}
