package com.qz.sns.sv.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qz.sns.model.entity.FamilyRelation;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 家庭关系表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Mapper
public interface FamilyRelationMapper extends BaseMapper<FamilyRelation> {

    @Select("SELECT fr.*, u.nickname, u.avatar, u.avatar_thumb FROM family_relation fr " +
            "LEFT JOIN user u ON fr.relative_id = u.id " +
            "WHERE fr.user_id = #{userId}")
    List<Map<String, Object>> getRelationsWithUserInfo(@Param("userId") Long userId);

    @Select("SELECT DISTINCT user_id FROM family_relation")
    List<Long> getAllUserIds();
    
    /**
     * 只获取家长用户的ID（role=1）
     */
    @Select("SELECT DISTINCT fr.user_id FROM family_relation fr " +
            "LEFT JOIN user u ON fr.user_id = u.id " +
            "WHERE u.role = 1")
    List<Long> getParentUserIds();

    /**
     * 获取用户所在的群组ID（优先获取最新的群组，假设为当前活跃的家庭群组）
     */
    @Select("SELECT group_id FROM im_group_member WHERE user_id = #{userId} AND quit = 0 ORDER BY group_id DESC LIMIT 1")
    Long getUserFamilyGroupId(@Param("userId") Long userId);
}
