package com.qz.sns.sv.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qz.sns.model.entity.LikeRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 点赞记录表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Repository
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {
    int insert(LikeRecord likeRecord);

    @Select("SELECT * FROM like_record WHERE user_id = #{userId} AND target_id = #{targetId} AND type = #{type}")
    LikeRecord selectByUserIdAndTarget(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("type") Integer type);

    @Delete("DELETE FROM like_record WHERE id = #{id}")
    void deleteById(Long id);

}
