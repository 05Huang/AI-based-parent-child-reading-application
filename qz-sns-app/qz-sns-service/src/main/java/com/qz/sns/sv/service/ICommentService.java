package com.qz.sns.sv.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.Comment;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * <p>
 * 评论表 服务类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
public interface ICommentService extends IService<Comment> {
    /**
     * 获取内容的根评论列表
     * @param contentId 内容ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页评论列表
     */
    PageDTO<CommentDTO> getContentComments(Long contentId, Integer page, Integer size);

    /**
     * 获取段落评论
     * @param contentId 内容ID
     * @param paragraphId 段落ID
     * @return 段落评论列表
     */
    CommentListDTO getParagraphComments(Long contentId, String paragraphId);

    /**
     * 获取评论回复列表
     * @param commentId 评论ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页回复列表
     */
    PageDTO<CommentDTO> getCommentReplies(Long commentId, Integer page, Integer size);

    /**
     * 添加评论
     * @param commentDTO 评论DTO
     * @return 新增评论DTO
     */
    CommentDTO addComment(CommentDTO commentDTO);

    /**
     * 获取内容段落评论计数
     * @param contentId 内容ID
     * @return 段落ID与评论数的映射
     */
    Map<String, Integer> getParagraphCommentCounts(Long contentId);

    /**
     * 根据ID获取评论详情
     *
     * @param id 评论ID
     * @return 评论详情
     */
    SimpleCommentResponseDTO getCommentById(Long id);

    /**
     * 分页查询评论列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageDTO<SimpleCommentResponseDTO> getCommentPage(SimpleCommentQueryDTO queryDTO);

    /**
     * 删除评论（逻辑删除，包含子评论和段落计数更新）
     *
     * @param id 评论ID
     * @param deleteReason 删除原因
     * @return 删除结果
     */
    boolean deleteComment(Long id, String deleteReason);

    /**
     * 批量删除评论
     *
     * @param ids 评论ID列表
     * @param deleteReason 删除原因
     * @return 删除结果统计
     */
    BatchDeleteResult batchDeleteComment(Long[] ids, String deleteReason);

    /**
     * 更新评论状态（包含子评论和段落计数更新）
     *
     * @param id 评论ID
     * @param status 状态
     * @return 更新结果
     */
    boolean updateCommentStatus(Long id, Integer status);

    /**
     * 批量更新评论状态
     *
     * @param ids 评论ID列表
     * @param status 目标状态
     * @return 批量更新结果
     */
    BatchUpdateResult batchUpdateCommentStatus(Long[] ids, Integer status);
}
