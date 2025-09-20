package com.qz.sns.sv.mapper;



import com.qz.sns.model.entity.ContentImage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 内容图片关联表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Repository
public interface ContentImageMapper extends BaseMapper<ContentImage> {
    /**
     * 根据内容ID查询图片，按排序号排序
     * @param contentId 内容ID
     * @return 图片列表
     */
    @Select("SELECT * FROM content_image WHERE content_id = #{contentId} ORDER BY sort_order ASC")
    List<ContentImage> selectByContentId(Long contentId);
    /**
     * 根据内容ID删除所有图片
     * @param contentId 内容ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM content_image WHERE content_id = #{contentId}")
    int deleteByContentId(@Param("contentId") Long contentId);

}
