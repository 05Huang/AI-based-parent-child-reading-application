package com.qz.sns.sv.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qz.sns.model.entity.PrivateMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 私聊消息 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {
    @Select("SELECT COUNT(*) FROM im_private_message " +
            "WHERE ((send_id = #{userId} AND recv_id = #{relativeId}) " +
            "OR (send_id = #{relativeId} AND recv_id = #{userId})) " +
            "AND send_time BETWEEN #{startTime} AND #{endTime}")
    int countPrivateMessagesInPeriod(@Param("userId") Long userId,
                                     @Param("relativeId") Long relativeId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    @Select("SELECT CAST(type AS SIGNED) AS type, COUNT(*) as count FROM im_private_message " +
            "WHERE ((send_id = #{userId} AND recv_id = #{relativeId}) " +
            "OR (send_id = #{relativeId} AND recv_id = #{userId})) " +
            "AND send_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY type")
    List<Map<String, Object>> countPrivateMessagesByType(@Param("userId") Long userId,
                                                         @Param("relativeId") Long relativeId,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

}
