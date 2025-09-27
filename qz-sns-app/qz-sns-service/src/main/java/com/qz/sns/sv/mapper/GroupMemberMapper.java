package com.qz.sns.sv.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qz.sns.model.entity.GroupMember;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 群成员 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
    @Select("SELECT group_id FROM im_group_member WHERE user_id = #{userId} AND quit = 0")
    List<Long> getUserGroupIds(@Param("userId") Long userId);

    @Select("SELECT user_id FROM im_group_member WHERE group_id = #{groupId} AND quit = 0")
    List<Long> getGroupMemberIds(@Param("groupId") Long groupId);
    
    /**
     * 获取群组中的家长成员ID（role=1）
     */
    @Select("SELECT gm.user_id FROM im_group_member gm " +
            "LEFT JOIN user u ON gm.user_id = u.id " +
            "WHERE gm.group_id = #{groupId} AND gm.quit = 0 AND u.role = 1")
    List<Long> getGroupParentMemberIds(@Param("groupId") Long groupId);

}
