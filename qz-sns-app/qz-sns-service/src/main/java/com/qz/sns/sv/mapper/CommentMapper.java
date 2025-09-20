package com.qz.sns.sv.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.dto.SimpleCommentQueryDTO;
import com.qz.sns.model.dto.SimpleCommentResponseDTO;
import com.qz.sns.model.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 评论表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Repository
public interface CommentMapper extends BaseMapper<Comment> {

    Comment selectById(Long id);

    int insert(Comment comment);

    @Select("SELECT * FROM comment WHERE content_id = #{contentId} AND parent_id = 0 AND comment_type = 1 AND status = 1 ORDER BY created_time DESC LIMIT #{offset}, #{size}")
    List<Comment> selectRootCommentsByContentId(@Param("contentId") Long contentId, @Param("offset") Integer offset, @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM comment WHERE content_id = #{contentId} AND parent_id = 0 AND comment_type = 1 AND status = 1")
    Long countRootCommentsByContentId(Long contentId);

    @Select("SELECT * FROM comment WHERE root_id = #{rootId} AND parent_id > 0 AND status = 1 ORDER BY created_time ASC LIMIT #{offset}, #{size}")
    List<Comment> selectRepliesByRootId(@Param("rootId") Long rootId, @Param("offset") Integer offset, @Param("size") Integer size);

    @Select("SELECT * FROM comment WHERE content_id = #{contentId} AND paragraph_id = #{paragraphId} AND parent_id = 0 AND comment_type = 2 AND status = 1 ORDER BY created_time DESC")
    List<Comment> selectParagraphComments(@Param("contentId") Long contentId, @Param("paragraphId") String paragraphId);

    @Select("SELECT * FROM comment WHERE parent_id = #{parentId} AND status = 1 ORDER BY created_time ASC LIMIT #{offset}, #{size}")
    List<Comment> selectRepliesByParentId(@Param("parentId") Long parentId, @Param("offset") Integer offset, @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM comment WHERE parent_id = #{parentId} AND status = 1")
    Long countRepliesByParentId(Long parentId);

    @Select("SELECT * FROM comment WHERE root_id = #{rootId} AND status = 1 ORDER BY created_time ASC")
    List<Comment> selectAllRepliesByCommentId(Long rootId);

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);

    @Update("UPDATE comment SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decrementLikeCount(Long id);

    @Update("UPDATE comment SET reply_count = reply_count + 1 WHERE id = #{id}")
    void incrementReplyCount(Long id);

    /**
     * 批量逻辑删除指定内容的所有评论
     *
     * @param contentId 内容ID
     * @return 影响行数
     */
    int deleteCommentsByContentId(@Param("contentId") Long contentId);

    /**
     * 批量逻辑删除多个内容的所有评论
     *
     * @param contentIds 内容ID列表
     * @return 影响行数
     */
    int deleteCommentsByContentIds(@Param("contentIds") Long[] contentIds);

    /**
     * 恢复指定内容的所有评论状态
     *
     * @param contentId 内容ID
     * @return 影响行数
     */
    int restoreCommentsByContentId(@Param("contentId") Long contentId);

    /**
     * 分页查询评论列表（包含内容标题和用户信息）
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<SimpleCommentResponseDTO> selectSimpleCommentPage(Page<SimpleCommentResponseDTO> page,
                                                            @Param("query") SimpleCommentQueryDTO queryDTO);

    /**
     * 根据ID获取评论详情（包含内容标题和用户信息）
     *
     * @param id 评论ID
     * @return 评论详情
     */
    SimpleCommentResponseDTO selectSimpleCommentById(@Param("id") Long id);

    /**
     * 根据ID获取评论详情（不过滤状态）
     *
     * @param id 评论ID
     * @return 评论详情
     */
    SimpleCommentResponseDTO selectSimpleCommentByIdWithoutStatusFilter(@Param("id") Long id);

    /**
     * 递归删除评论及其所有子评论
     *
     * @param commentId 评论ID
     * @return 影响行数
     */
    int deleteCommentAndReplies(@Param("commentId") Long commentId);

    /**
     * 递归恢复评论及其所有子评论
     *
     * @param commentId 评论ID
     * @return 影响行数
     */
    int restoreCommentAndReplies(@Param("commentId") Long commentId);

    /**
     * 获取评论的子评论列表（用于计算段落评论数变化）
     *
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    List<Comment> selectChildComments(@Param("parentId") Long parentId);



}
