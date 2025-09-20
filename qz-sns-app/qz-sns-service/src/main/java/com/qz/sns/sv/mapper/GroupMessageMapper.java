package com.qz.sns.sv.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qz.sns.model.entity.GroupMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 群消息 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Mapper
public interface GroupMessageMapper extends BaseMapper<GroupMessage> {
    @Select("SELECT COUNT(*) FROM im_group_message " +
            "WHERE group_id = #{groupId} AND send_id = #{userId} " +
            "AND send_time BETWEEN #{startTime} AND #{endTime}")
    int countGroupMessagesInPeriod(@Param("groupId") Long groupId,
                                   @Param("userId") Long userId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    @Select("SELECT CAST(type AS SIGNED) AS type, COUNT(*) as count FROM im_group_message " +
            "WHERE group_id = #{groupId} AND send_id = #{userId} " +
            "AND send_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY type")
    List<Map<String, Object>> countGroupMessagesByType(@Param("groupId") Long groupId,
                                                       @Param("userId") Long userId,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    @Select("SELECT send_id, COUNT(*) as msg_count FROM im_group_message " +
            "WHERE group_id = #{groupId} " +
            "AND send_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY send_id")
    List<Map<String, Object>> countGroupMessagesBySender(@Param("groupId") Long groupId,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);
}
