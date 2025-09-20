package com.qz.sns.sv.mapper;

import com.qz.sns.model.entity.ParagraphCommentCount;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 段落评论计数表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-04-17
 */
@Repository
public interface ParagraphCommentCountMapper extends BaseMapper<ParagraphCommentCount> {
    int insert(ParagraphCommentCount count);

    @Select("SELECT * FROM paragraph_comment_count WHERE content_id = #{contentId}")
    List<ParagraphCommentCount> selectByContentId(Long contentId);

    @Select("SELECT * FROM paragraph_comment_count WHERE content_id = #{contentId} AND paragraph_id = #{paragraphId}")
    ParagraphCommentCount selectByContentIdAndParagraphId(@Param("contentId") Long contentId, @Param("paragraphId") String paragraphId);

    @Update("UPDATE paragraph_comment_count SET comment_count = comment_count + 1 WHERE content_id = #{contentId} AND paragraph_id = #{paragraphId}")
    int incrementCommentCount(@Param("contentId") Long contentId, @Param("paragraphId") String paragraphId);

    /**
     * 更新段落评论计数
     *
     * @param contentId 内容ID
     * @param paragraphId 段落ID
     * @param increment 增量（可为负数）
     * @return 影响行数
     */
    int updateCommentCount(@Param("contentId") Long contentId,
                           @Param("paragraphId") String paragraphId,
                           @Param("increment") Integer increment);

    /**
     * 获取段落评论计数
     *
     * @param contentId 内容ID
     * @param paragraphId 段落ID
     * @return 评论计数
     */
    Integer getCommentCount(@Param("contentId") Long contentId,
                            @Param("paragraphId") String paragraphId);
}
