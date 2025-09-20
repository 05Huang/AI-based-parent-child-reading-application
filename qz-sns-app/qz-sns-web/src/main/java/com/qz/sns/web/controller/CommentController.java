package com.qz.sns.web.controller;

import com.qz.sns.model.dto.*;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.impl.CommentServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 评论表 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentServiceImpl commentService;

    /**
     * 获取文章的普通评论列表
     * @param contentId 文章ID
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表，包含回复
     */
    @GetMapping("/content/{contentId}")
    public Result<PageDTO<CommentDTO>> getContentComments(
            @PathVariable Long contentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<CommentDTO> comments = commentService.getContentComments(contentId, page, size);
        return ResultUtils.success(comments);
    }

    /**
     * 获取段落评论列表
     * @param contentId 文章ID
     * @param paragraphId 段落ID
     * @return 段落评论列表
     */
    @GetMapping("/paragraph")
    public Result<CommentListDTO> getParagraphComments(
            @RequestParam Long contentId,
            @RequestParam String paragraphId) {
        CommentListDTO comments = commentService.getParagraphComments(contentId, paragraphId);
        return ResultUtils.success(comments);
    }

    /**
     * 获取评论的回复列表
     * @param commentId 评论ID
     * @param page 页码
     * @param size 每页大小
     * @return 回复列表
     */
    @GetMapping("/{commentId}/replies")
    public Result<PageDTO<CommentDTO>> getCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageDTO<CommentDTO> replies = commentService.getCommentReplies(commentId, page, size);
        return ResultUtils.success(replies);
    }

    /**
     * 发表评论
     * @param comment 评论内容
     * @return 新创建的评论
     */
    @PostMapping
    public Result<CommentDTO> addComment(@RequestBody CommentDTO comment) {
        CommentDTO newComment = commentService.addComment(comment);
        return ResultUtils.success(newComment);
    }

    /**
     * 获取文章所有段落的评论计数
     * @param contentId 文章ID
     * @return 段落评论计数映射
     */
    @GetMapping("/paragraph/counts")
    public Result<Object> getParagraphCommentCounts(@RequestParam Long contentId) {
        Object counts = commentService.getParagraphCommentCounts(contentId);
        return ResultUtils.success(counts);
    }

    /**
     * 根据ID获取评论详情
     *
     * @param id 评论ID
     * @return 评论详情
     */
    @GetMapping("/{id}")
    public Result<SimpleCommentResponseDTO> getCommentById(@PathVariable @NotNull(message = "评论ID不能为空") Long id) {
        log.info("接收获取评论详情请求，评论ID：{}", id);

        try {
            SimpleCommentResponseDTO comment = commentService.getCommentById(id);
            return ResultUtils.success(comment);
        } catch (Exception e) {
            log.error("获取评论详情失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取评论详情失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询评论列表
     * 支持通过status参数查询不同状态的评论：
     * - status=1: 查询正常评论（默认）
     * - status=0: 查询已删除评论
     * - 不传status: 默认查询正常评论
     *
     * @param queryDTO 查询条件
     * @return 分页评论列表
     */
    @GetMapping("/page")
    public Result<PageDTO<SimpleCommentResponseDTO>> getCommentPage(@Valid SimpleCommentQueryDTO queryDTO) {
        log.info("接收分页查询评论请求，查询条件：{}", queryDTO);

        try {
            PageDTO<SimpleCommentResponseDTO> pageResult = commentService.getCommentPage(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("分页查询评论失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询评论列表失败：" + e.getMessage());
        }
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @param deleteReason 删除原因
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @PathVariable @NotNull(message = "评论ID不能为空") Long id,
            @RequestParam(required = false) String deleteReason) {

        log.info("接收删除评论请求，评论ID：{}，删除原因：{}", id, deleteReason);

        try {
            boolean success = commentService.deleteComment(id, deleteReason);

            if (success) {
                return ResultUtils.success("评论删除成功");
            } else {
                return ResultUtils.error(500, "评论删除失败");
            }
        } catch (Exception e) {
            log.error("删除评论失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "删除评论失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除评论
     *
     * @param ids 评论ID列表
     * @param deleteReason 删除原因
     * @return 批量删除结果
     */
    @DeleteMapping("/batch")
    public Result<BatchDeleteResult> batchDeleteComment(
            @RequestBody @NotEmpty(message = "评论ID列表不能为空") Long[] ids,
            @RequestParam(required = false) String deleteReason) {

        log.info("接收批量删除评论请求，评论数量：{}，删除原因：{}", ids.length, deleteReason);

        try {
            BatchDeleteResult result = commentService.batchDeleteComment(ids, deleteReason);

            if (result.isAllSuccess()) {
                return ResultUtils.success(result, "批量删除成功");
            } else {
                return ResultUtils.success(result,
                        String.format("批量删除完成，成功：%d，失败：%d",
                                result.getSuccessCount(), result.getFailCount()));
            }
        } catch (Exception e) {
            log.error("批量删除评论失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "批量删除评论失败：" + e.getMessage());
        }
    }

    /**
     * 更新评论状态
     *
     * @param id 评论ID
     * @param status 状态
     * @return 更新结果
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateCommentStatus(
            @PathVariable @NotNull(message = "评论ID不能为空") Long id,
            @RequestParam @NotNull(message = "状态值不能为空") Integer status) {

        log.info("接收更新评论状态请求，评论ID：{}，状态：{}", id, status);

        try {
            boolean success = commentService.updateCommentStatus(id, status);

            if (success) {
                String statusText = status == 1 ? "恢复" : "删除";
                return ResultUtils.success("评论" + statusText + "成功");
            } else {
                return ResultUtils.error(500, "评论状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新评论状态失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "更新评论状态失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新评论状态
     *
     * @param ids 评论ID列表
     * @param status 状态
     * @return 批量更新结果
     */
    @PatchMapping("/batch/status")
    public Result<BatchUpdateResult> batchUpdateCommentStatus(
            @RequestBody @NotEmpty(message = "评论ID列表不能为空") Long[] ids,
            @RequestParam @NotNull(message = "状态值不能为空") Integer status) {

        log.info("接收批量更新评论状态请求，评论数量：{}，状态：{}", ids.length, status);

        try {
            BatchUpdateResult result = commentService.batchUpdateCommentStatus(ids, status);

            String statusText = status == 1 ? "恢复" : "删除";
            if (result.isAllSuccess()) {
                return ResultUtils.success(result, "批量" + statusText + "成功");
            } else {
                return ResultUtils.success(result,
                        String.format("批量%s完成，成功：%d，失败：%d",
                                statusText, result.getSuccessCount(), result.getFailCount()));
            }
        } catch (Exception e) {
            log.error("批量更新评论状态失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "批量更新评论状态失败：" + e.getMessage());
        }
    }

    /**
     * 便捷的已删除评论查询接口
     *
     * @param current 页码
     * @param size 每页大小
     * @param contentId 内容ID
     * @param userId 用户ID
     * @param content 评论内容关键词
     * @return 已删除评论列表
     */
    @GetMapping("/deleted")
    public Result<PageDTO<SimpleCommentResponseDTO>> getDeletedComments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long contentId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String content) {

        log.info("接收已删除评论查询请求，页码：{}，大小：{}，内容ID：{}，用户ID：{}，关键词：{}",
                current, size, contentId, userId, content);

        try {
            SimpleCommentQueryDTO queryDTO = new SimpleCommentQueryDTO();
            queryDTO.setCurrent(current);
            queryDTO.setSize(size);
            queryDTO.setStatus(0); // 查询已删除状态
            queryDTO.setContentId(contentId);
            queryDTO.setUserId(userId);
            queryDTO.setContent(content);
            queryDTO.setSortField("created_time");
            queryDTO.setSortOrder("desc");

            PageDTO<SimpleCommentResponseDTO> pageResult = commentService.getCommentPage(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("查询已删除评论失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询已删除评论失败：" + e.getMessage());
        }
    }

    /**
     * 根据内容ID查询评论
     *
     * @param contentId 内容ID
     * @param current 页码
     * @param size 每页大小
     * @param commentType 评论类型
     * @param paragraphId 段落ID
     * @return 评论列表
     */
    @GetMapping("/contentby/{contentId}")
    public Result<PageDTO<SimpleCommentResponseDTO>> getCommentsByContentId(
            @PathVariable @NotNull(message = "内容ID不能为空") Long contentId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer commentType,
            @RequestParam(required = false) String paragraphId) {

        log.info("接收根据内容ID查询评论请求，内容ID：{}，页码：{}，大小：{}，类型：{}，段落ID：{}",
                contentId, current, size, commentType, paragraphId);

        try {
            SimpleCommentQueryDTO queryDTO = new SimpleCommentQueryDTO();
            queryDTO.setCurrent(current);
            queryDTO.setSize(size);
            queryDTO.setContentId(contentId);
            queryDTO.setCommentType(commentType);
            queryDTO.setParagraphId(paragraphId);
            queryDTO.setSortField("created_time");
            queryDTO.setSortOrder("desc");

            PageDTO<SimpleCommentResponseDTO> pageResult = commentService.getCommentPage(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("根据内容ID查询评论失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询评论失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询评论
     *
     * @param userId 用户ID
     * @param current 页码
     * @param size 每页大小
     * @return 评论列表
     */
    @GetMapping("/user/{userId}")
    public Result<PageDTO<SimpleCommentResponseDTO>> getCommentsByUserId(
            @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("接收根据用户ID查询评论请求，用户ID：{}，页码：{}，大小：{}", userId, current, size);

        try {
            SimpleCommentQueryDTO queryDTO = new SimpleCommentQueryDTO();
            queryDTO.setCurrent(current);
            queryDTO.setSize(size);
            queryDTO.setUserId(userId);
            queryDTO.setSortField("created_time");
            queryDTO.setSortOrder("desc");

            PageDTO<SimpleCommentResponseDTO> pageResult = commentService.getCommentPage(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("根据用户ID查询评论失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询用户评论失败：" + e.getMessage());
        }
    }
}