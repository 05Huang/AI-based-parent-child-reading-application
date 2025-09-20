package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.common.utils.BeanUtils;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.Comment;
import com.qz.sns.model.entity.ParagraphCommentCount;
import com.qz.sns.model.entity.User;
import com.qz.sns.sv.mapper.CommentMapper;
import com.qz.sns.sv.mapper.ContentMapper;
import com.qz.sns.sv.mapper.ParagraphCommentCountMapper;
import com.qz.sns.sv.mapper.UserMapper;
import com.qz.sns.sv.service.ICommentService;
import com.qz.sns.sv.session.SessionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 评论表 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private ParagraphCommentCountMapper paragraphCommentCountMapper;

    @Autowired
    private LikeRecordServiceImpl likeService;

    @Override
    public PageDTO<CommentDTO> getContentComments(Long contentId, Integer page, Integer size) {
        // 1. 获取根评论列表
        int offset = (page - 1) * size;
        List<Comment> comments = commentMapper.selectRootCommentsByContentId(contentId, offset, size);
        Long total = commentMapper.countRootCommentsByContentId(contentId);

        // 2. 构建评论DTO列表
        List<CommentDTO> commentDTOs = buildCommentDTOList(comments);

        // 3. 获取每个评论的部分回复
        for (CommentDTO commentDTO : commentDTOs) {
            List<Comment> replies = commentMapper.selectRepliesByRootId(commentDTO.getId(), 0, 3);
            commentDTO.setReplies(buildCommentDTOList(replies));
        }

        // 4. 构建分页对象
        PageDTO<CommentDTO> pageDTO = new PageDTO<>();
        pageDTO.setRecords(commentDTOs);
        pageDTO.setTotal(total);
        pageDTO.setCurrent(page);
        pageDTO.setSize(size);

        return pageDTO;
    }

    @Override
    public CommentListDTO getParagraphComments(Long contentId, String paragraphId) {
        // 1. 获取段落评论列表
        List<Comment> comments = commentMapper.selectParagraphComments(contentId, paragraphId);

        // 2. 构建评论DTO列表
        List<CommentDTO> commentDTOs = buildCommentDTOList(comments);

        // 3. 获取每个评论的所有回复
        for (CommentDTO commentDTO : commentDTOs) {
            List<Comment> replies = commentMapper.selectAllRepliesByCommentId(commentDTO.getId());
            commentDTO.setReplies(buildCommentDTOList(replies));
        }

        // 4. 构建返回对象
        CommentListDTO listDTO = new CommentListDTO();
        listDTO.setComments(commentDTOs);
        listDTO.setTotal((long) commentDTOs.size());

        return listDTO;
    }

    @Override
    public PageDTO<CommentDTO> getCommentReplies(Long commentId, Integer page, Integer size) {
        // 1. 获取评论回复列表
        int offset = (page - 1) * size;
        List<Comment> replies = commentMapper.selectRepliesByParentId(commentId, offset, size);
        Long total = commentMapper.countRepliesByParentId(commentId);

        // 2. 构建回复DTO列表
        List<CommentDTO> replyDTOs = buildCommentDTOList(replies);

        // 3. 构建分页对象
        PageDTO<CommentDTO> pageDTO = new PageDTO<>();
        pageDTO.setRecords(replyDTOs);
        pageDTO.setTotal(total);
        pageDTO.setCurrent(page);
        pageDTO.setSize(size);

        return pageDTO;
    }

    @Override
    @Transactional
    public CommentDTO addComment(CommentDTO commentDTO) {
        // 1. 组装评论实体
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);

        // 2. 设置评论状态
        comment.setStatus(1);
        comment.setLikeCount(0);
        comment.setReplyCount(0);

        // 3. 保存评论
        commentMapper.insert(comment);

        // 4. 更新评论计数
        if (comment.getCommentType() == 1) {
            // 普通评论
            if (comment.getParentId() == 0) {
                // 根评论，更新内容评论数
                contentMapper.incrementCommentCount(comment.getContentId());
            } else {
                // 回复，更新父评论回复数
                commentMapper.incrementReplyCount(comment.getParentId());
                // 同时也要更新内容评论数
                contentMapper.incrementCommentCount(comment.getContentId());
            }
        } else if (comment.getCommentType() == 2) {
            // 段落评论
            if (comment.getParentId() == 0) {
                // 根评论，更新段落评论数
                updateParagraphCommentCount(comment.getContentId(), comment.getParagraphId());
                // 同时更新内容评论数
                contentMapper.incrementCommentCount(comment.getContentId());
            } else {
                // 回复，更新父评论回复数
                commentMapper.incrementReplyCount(comment.getParentId());
                // 同时更新段落评论数和内容评论数
                updateParagraphCommentCount(comment.getContentId(), comment.getParagraphId());
                contentMapper.incrementCommentCount(comment.getContentId());
            }
        }

        // 5. 获取用户信息
        User user = userMapper.selectById(comment.getUserId());

        // 6. 构建返回对象
        CommentDTO resultDTO = new CommentDTO();
        BeanUtils.copyProperties(comment, resultDTO);
        resultDTO.setUserName(user.getNickname());
        resultDTO.setUserAvatar(user.getAvatar());

        // 如果是回复，需要设置回复对象的名称
        if (comment.getParentId() > 0) {
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                User replyToUser = userMapper.selectById(parentComment.getUserId());
                resultDTO.setReplyToName(replyToUser.getNickname());
            }
        }

        return resultDTO;
    }

    @Override
    public Map<String, Integer> getParagraphCommentCounts(Long contentId) {
        List<ParagraphCommentCount> counts = paragraphCommentCountMapper.selectByContentId(contentId);
        Map<String, Integer> result = new HashMap<>();

        for (ParagraphCommentCount count : counts) {
            result.put(count.getParagraphId(), count.getCommentCount());
        }

        return result;
    }

    /**
     * 构建评论DTO列表
     * @param comments 评论实体列表
     * @return 评论DTO列表
     */
    private List<CommentDTO> buildCommentDTOList(List<Comment> comments) {
        // 获取当前登录用户
        Long currentUserId = getCurrentUserId();

        return comments.stream().map(comment -> {
            CommentDTO dto = new CommentDTO();
            BeanUtils.copyProperties(comment, dto);

            // 获取评论用户信息
            User user = userMapper.selectById(comment.getUserId());
            dto.setUserName(user.getNickname());
            dto.setUserAvatar(user.getAvatar());

            // 设置当前用户是否已点赞
            boolean isLiked = likeService.getLikeStatus(currentUserId, comment.getId(), 2);
            dto.setIsLiked(isLiked);

            // 如果是回复评论，设置回复对象的名称
            if (comment.getParentId() > 0) {
                Comment parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null) {
                    User replyToUser = userMapper.selectById(parentComment.getUserId());
                    dto.setReplyToName(replyToUser.getNickname());
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 更新段落评论计数
     * @param contentId 内容ID
     * @param paragraphId 段落ID
     */
    private void updateParagraphCommentCount(Long contentId, String paragraphId) {
        ParagraphCommentCount count = paragraphCommentCountMapper.selectByContentIdAndParagraphId(contentId, paragraphId);

        if (count == null) {
            // 不存在记录，创建新记录
            count = new ParagraphCommentCount();
            count.setContentId(contentId);
            count.setParagraphId(paragraphId);
            count.setCommentCount(1);
            paragraphCommentCountMapper.insert(count);
        } else {
            // 已存在记录，更新计数
            paragraphCommentCountMapper.incrementCommentCount(contentId, paragraphId);
        }
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        if (SessionContext.getSession() != null) {
            return SessionContext.getSession().getUserId();
        }
        return null;
    }

    /**
     * 根据ID获取评论详情
     */
    @Override
    public SimpleCommentResponseDTO getCommentById(Long id) {
        log.info("获取评论详情，评论ID：{}", id);

        if (id == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }

        SimpleCommentResponseDTO comment = commentMapper.selectSimpleCommentById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在或已被删除");
        }

        return comment;
    }

    /**
     * 分页查询评论列表
     */
    @Override
    public PageDTO<SimpleCommentResponseDTO> getCommentPage(SimpleCommentQueryDTO queryDTO) {
        log.info("分页查询评论列表，查询条件：{}", queryDTO);

        // 构建分页对象
        Page<SimpleCommentResponseDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 执行分页查询
        IPage<SimpleCommentResponseDTO> commentPage = commentMapper.selectSimpleCommentPage(page, queryDTO);

        // 构建返回结果
        PageDTO<SimpleCommentResponseDTO> result = new PageDTO<>();
        result.setRecords(commentPage.getRecords());
        result.setTotal(commentPage.getTotal());
        result.setCurrent((int) commentPage.getCurrent());
        result.setSize((int) commentPage.getSize());

        log.info("评论列表查询完成，共查询到{}条记录", result.getTotal());
        return result;
    }

    /**
     * 删除评论（逻辑删除，包含子评论和段落计数更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long id, String deleteReason) {
        log.info("开始删除评论，评论ID：{}，删除原因：{}", id, deleteReason);

        // 验证评论是否存在
        SimpleCommentResponseDTO comment = commentMapper.selectSimpleCommentByIdWithoutStatusFilter(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        if (comment.getStatus() == 0) {
            log.info("评论已被删除，无需重复操作，评论ID：{}", id);
            return true;
        }

        try {
            // 1. 获取所有子评论（用于计算段落评论数变化）
            List<Comment> childComments = commentMapper.selectChildComments(id);

            // 2. 逻辑删除评论及其所有子评论
            int deletedCount = commentMapper.deleteCommentAndReplies(id);

            // 3. 更新段落评论计数（如果是段落评论）
            if (comment.getCommentType() == 2 && StringUtils.hasText(comment.getParagraphId())) {
                // 计算需要减少的评论数（包括当前评论和所有子评论）
                int decrementCount = 1; // 当前评论
                for (Comment child : childComments) {
                    if (child.getStatus() == 1) { // 只计算正常状态的子评论
                        decrementCount++;
                    }
                }

                // 更新段落评论计数
                paragraphCommentCountMapper.updateCommentCount(
                        comment.getContentId(),
                        comment.getParagraphId(),
                        -decrementCount
                );

                log.info("更新段落评论计数，内容ID：{}，段落ID：{}，减少：{}条",
                        comment.getContentId(), comment.getParagraphId(), decrementCount);
            }

            log.info("评论删除完成，评论ID：{}，删除评论数：{}", id, deletedCount);
            return deletedCount > 0;

        } catch (Exception e) {
            log.error("删除评论失败，评论ID：{}，错误：{}", id, e.getMessage(), e);
            throw new RuntimeException("删除评论失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchDeleteResult batchDeleteComment(Long[] ids, String deleteReason) {
        log.info("开始批量删除评论，评论数量：{}，删除原因：{}", ids.length, deleteReason);

        BatchDeleteResult result = new BatchDeleteResult();
        int successCount = 0;
        int failCount = 0;

        for (Long id : ids) {
            try {
                boolean success = deleteComment(id, deleteReason);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("删除评论失败，评论ID：{}，错误：{}", id, e.getMessage());
                failCount++;
            }
        }

        log.info("批量删除评论完成，成功：{}，失败：{}", successCount, failCount);

        result.setTotalCount(ids.length);
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setErrorMessage(failCount > 0 ? "部分评论删除失败" : null);

        return result;
    }

    /**
     * 更新评论状态（包含子评论和段落计数更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCommentStatus(Long id, Integer status) {
        log.info("更新评论状态，评论ID：{}，目标状态：{}", id, status);

        if (id == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }

        // 验证评论是否存在
        SimpleCommentResponseDTO comment = commentMapper.selectSimpleCommentByIdWithoutStatusFilter(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查状态是否需要更新
        if (comment.getStatus().equals(status)) {
            log.info("评论状态无需更新，评论ID：{}，当前状态：{}", id, status);
            return true;
        }

        try {
            // 1. 获取所有子评论（用于计算段落评论数变化）
            List<Comment> childComments = commentMapper.selectChildComments(id);

            // 2. 更新评论及其所有子评论的状态
            int updatedCount = 0;
            if (status == 1) {
                // 恢复评论
                updatedCount = commentMapper.restoreCommentAndReplies(id);
            } else {
                // 删除评论
                updatedCount = commentMapper.deleteCommentAndReplies(id);
            }

            // 3. 更新段落评论计数（如果是段落评论）
            if (comment.getCommentType() == 2 && StringUtils.hasText(comment.getParagraphId())) {
                // 计算评论数变化
                int countChange = status == 1 ? 1 : -1; // 当前评论的变化

                // 计算子评论的变化
                for (Comment child : childComments) {
                    if ((status == 1 && child.getStatus() == 0) ||
                            (status == 0 && child.getStatus() == 1)) {
                        countChange += (status == 1 ? 1 : -1);
                    }
                }

                // 更新段落评论计数
                paragraphCommentCountMapper.updateCommentCount(
                        comment.getContentId(),
                        comment.getParagraphId(),
                        countChange
                );

                String statusText = status == 1 ? "恢复" : "删除";
                log.info("更新段落评论计数，内容ID：{}，段落ID：{}，{}：{}条",
                        comment.getContentId(), comment.getParagraphId(), statusText, Math.abs(countChange));
            }

            String statusText = status == 1 ? "恢复" : "删除";
            log.info("评论状态更新成功，评论ID：{}，状态：{}，影响评论数：{}", id, statusText, updatedCount);

            return updatedCount > 0;

        } catch (Exception e) {
            log.error("更新评论状态失败，评论ID：{}，错误：{}", id, e.getMessage(), e);
            throw new RuntimeException("更新评论状态失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新评论状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchUpdateResult batchUpdateCommentStatus(Long[] ids, Integer status) {
        log.info("批量更新评论状态，评论数量：{}，目标状态：{}", ids.length, status);

        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException("评论ID列表不能为空");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }

        BatchUpdateResult result = new BatchUpdateResult();
        int successCount = 0;
        int failCount = 0;
        for (Long id : ids) {
            try {
                boolean success = updateCommentStatus(id, status);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("更新评论状态失败，评论ID：{}，错误：{}", id, e.getMessage());
                failCount++;
            }
        }

        String statusText = status == 1 ? "恢复" : "删除";
        log.info("批量{}评论完成，成功：{}，失败：{}", statusText, successCount, failCount);

        result.setTotalCount(ids.length);
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setErrorMessage(failCount > 0 ? "部分评论状态更新失败" : null);

        return result;
    }
}