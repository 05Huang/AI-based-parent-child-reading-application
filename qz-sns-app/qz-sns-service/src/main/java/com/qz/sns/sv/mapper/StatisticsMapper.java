package com.qz.sns.sv.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qz.sns.model.entity.Statistics;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 用户统计数据表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Repository
public interface StatisticsMapper extends BaseMapper<Statistics> {

    @Select("SELECT weekly_read_target_minutes FROM user_statistics WHERE user_id = #{userId}")
    Integer getWeeklyReadTargetMinutes(@Param("userId") Long userId);

    @Update("INSERT INTO user_statistics (user_id, weekly_read_target_minutes) " +
            "VALUES (#{userId}, #{targetMinutes}) " +
            "ON DUPLICATE KEY UPDATE weekly_read_target_minutes = VALUES(weekly_read_target_minutes)")
    int upsertWeeklyReadTargetMinutes(@Param("userId") Long userId, @Param("targetMinutes") Integer targetMinutes);
}
