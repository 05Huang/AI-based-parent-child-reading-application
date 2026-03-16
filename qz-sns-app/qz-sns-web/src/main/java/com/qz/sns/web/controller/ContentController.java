package com.qz.sns.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.common.constant.UserConstant;
import com.qz.sns.common.enums.UserType;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.vo.ContentResponse;
import com.qz.sns.sv.annotation.RequirePermission;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.impl.ContentServiceImpl;
import com.qz.sns.sv.session.SessionContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;


/**
 * <p>
 * 内容表 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private ContentServiceImpl contentService;

    /**
     * 创建内容
     */
    @PostMapping
    public Result<ContentResponse> createContent(
            @RequestBody ContentDTO request,
            @RequestParam(required = false) Long userId) {
        try {
            // 如果没有提供userId，使用测试用户ID
            Long effectiveUserId = userId != null ? userId : UserConstant.TEST_USER_ID;
            System.out.println("当前用户ID: " + effectiveUserId);
            System.out.println("当前的内容请求: " + request);
            ContentResponse content = contentService.createContent(request, effectiveUserId);
            return ResultUtils.success(content, "创建内容成功");
        } catch (Exception e) {
            log.error("创建内容失败", e);
            return ResultUtils.error(400, "创建内容失败: " + e.getMessage());
        }
    }

    /**
     * 更新内容
     */
    @PutMapping("/{id}")
    public Result<ContentResponse> updateContent(
            @PathVariable Long id,
            @RequestBody ContentDTO request,
            @RequestParam(required = false) Long userId) {
        try {
            // 如果没有提供userId，使用测试用户ID
            Long effectiveUserId = userId != null ? userId : UserConstant.TEST_USER_ID;
            ContentResponse content = contentService.updateContent(id, request, effectiveUserId);
            return ResultUtils.success(content, "更新内容成功");
        } catch (Exception e) {
            log.error("更新内容失败", e);
            return ResultUtils.error(400, "更新内容失败: " + e.getMessage());
        }
    }

    /**
     * 获取文章详情
     * @param id 文章ID
     * @return 文章详情，包含作者信息
     */
    @GetMapping("/{id}")
    public Result<ContentRequest> getContentDetail(@PathVariable Long id) {
        try {
            ContentRequest content = contentService.getContentDetail(id);
            return ResultUtils.success(content);
        } catch (Exception e) {
            log.error("获取内容详情失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取内容详情失败：" + e.getMessage());
        }
    }

    /**
     * 增加文章浏览量
     * @param id 文章ID
     * @return 操作结果
     */
    @PostMapping("/{id}/view")
    public Result<Void> incrementViewCount(@PathVariable Long id) {
        contentService.incrementViewCount(id);
        return ResultUtils.success();
    }

    /**
     * 分页查询内容列表
     *
     * @param queryDTO 查询条件
     * @return 分页内容列表
     */
    @GetMapping("/page")
    public Result<PageDTO<ContentRequest>> getContentPage(@Valid ContentQueryDTO queryDTO) {
        log.info("接收分页查询内容请求，查询条件：{}", queryDTO);

        try {
            PageDTO<ContentRequest> pageResult = contentService.getContentPage(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("分页查询内容失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询内容列表失败：" + e.getMessage());
        }
    }

    /**
     * 删除内容
     *
     * @param deleteDTO 删除请求
     * @return 删除结果
     */
    @RequirePermission(value = UserType.ADMIN)
    @DeleteMapping
    public Result<Void> deleteContent(@Valid @RequestBody ContentDeleteDTO deleteDTO) {
        log.info("接收删除内容请求，内容ID：{}", deleteDTO.getId());

        try {
            boolean success = contentService.deleteContent(deleteDTO);

            if (success) {
                return ResultUtils.success("内容删除成功");
            } else {
                return ResultUtils.error(500, "内容删除失败");
            }
        } catch (Exception e) {
            log.error("删除内容失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "删除内容失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除内容
     *
     * @param batchDeleteDTO 批量删除请求
     * @return 批量删除结果
     */
    @DeleteMapping("/batch")
    public Result<BatchDeleteResult> batchDeleteContent(@Valid @RequestBody ContentBatchDeleteDTO batchDeleteDTO) {
        log.info("接收批量删除内容请求，内容数量：{}", batchDeleteDTO.getIds().length);

        try {
            BatchDeleteResult result = contentService.batchDeleteContent(batchDeleteDTO);

            if (result.isAllSuccess()) {
                return ResultUtils.success(result, "批量删除成功");
            } else {
                return ResultUtils.success(result,
                        String.format("批量删除完成，成功：%d，失败：%d",
                                result.getSuccessCount(), result.getFailCount()));
            }
        } catch (Exception e) {
            log.error("批量删除内容失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "批量删除内容失败：" + e.getMessage());
        }
    }

    /**
     * 搜索内容
     *
     * @param keyword 搜索关键词
     * @param current 页码
     * @param size 每页大小
     * @param type 内容类型（可选）
     * @return 搜索结果
     */
    @GetMapping("/search")
    public Result<PageDTO<ContentRequest>> searchContent(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") String keyword,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size,
            @RequestParam(required = false) Integer type) {

        log.info("接收搜索内容请求，关键词：{}，页码：{}，每页大小：{}，类型：{}", keyword, current, size, type);

        try {
            PageDTO<ContentRequest> pageResult = contentService.searchContent(keyword, current, size, type);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("搜索内容失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "搜索内容失败：" + e.getMessage());
        }
    }

    /**
     * 获取热门内容
     *
     * @param limit 限制数量
     * @return 热门内容列表
     */
    @GetMapping("/hot")
    public Result<List<ContentRequest>> getHotContent(
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "限制数量必须大于0") Integer limit) {

        log.info("接收获取热门内容请求，限制数量：{}", limit);

        try {
            List<ContentRequest> hotContent = contentService.getHotContent(limit);
            return ResultUtils.success(hotContent);
        } catch (Exception e) {
            log.error("获取热门内容失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取热门内容失败：" + e.getMessage());
        }
    }

    /**
     * 根据分类获取内容
     *
     * @param categoryId 分类ID
     * @param limit 限制数量
     * @return 内容列表
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<ContentRequest>> getContentByCategory(
            @PathVariable @NotNull(message = "分类ID不能为空") Long categoryId,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "限制数量必须大于0") Integer limit) {

        log.info("接收根据分类获取内容请求，分类ID：{}，限制数量：{}", categoryId, limit);

        try {
            List<ContentRequest> content = contentService.getContentByCategory(categoryId, limit);
            return ResultUtils.success(content);
        } catch (Exception e) {
            log.error("根据分类获取内容失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取分类内容失败：" + e.getMessage());
        }
    }

    /**
     * 根据创建者获取内容
     *
     * @param creatorId 创建者ID
     * @param current 页码
     * @param size 每页大小
     * @return 分页内容列表
     */
    @GetMapping("/creator/{creatorId}")
    public Result<PageDTO<ContentRequest>> getContentByCreator(
            @PathVariable @NotNull(message = "创建者ID不能为空") Long creatorId,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size) {

        log.info("接收根据创建者获取内容请求，创建者ID：{}，页码：{}，每页大小：{}", creatorId, current, size);

        try {
            PageDTO<ContentRequest> pageResult = contentService.getContentByCreator(creatorId, current, size);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("根据创建者获取内容失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取创建者内容失败：" + e.getMessage());
        }
    }

    /**
     * 根据标签获取内容
     *
     * @param tag 标签
     * @param current 页码
     * @param size 每页大小
     * @return 分页内容列表
     */
    @GetMapping("/tag")
    public Result<PageDTO<ContentRequest>> getContentByTag(
            @RequestParam @NotBlank(message = "标签不能为空") String tag,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小必须大于0") Integer size) {

        log.info("接收根据标签获取内容请求，标签：{}，页码：{}，每页大小：{}", tag, current, size);

        try {
            PageDTO<ContentRequest> pageResult = contentService.getContentByTag(tag, current, size);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("根据标签获取内容失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取标签内容失败：" + e.getMessage());
        }
    }

    /**
     * 更新内容状态
     *
     * @param id 内容ID
     * @param status 状态
     * @return 更新结果
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateContentStatus(
            @PathVariable @NotNull(message = "内容ID不能为空") Long id,
            @RequestParam @NotNull(message = "状态值不能为空") Integer status) {

        log.info("接收更新内容状态请求，内容ID：{}，状态：{}", id, status);

        try {
            boolean success = contentService.updateContentStatus(id, status);

            if (success) {
                String statusText = status == 1 ? "上架" : "下架";
                return ResultUtils.success("内容" + statusText + "成功");
            } else {
                return ResultUtils.error(500, "内容状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新内容状态失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "更新内容状态失败：" + e.getMessage());
        }
    }

}
