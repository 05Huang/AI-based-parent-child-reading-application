package com.qz.sns.web.controller;

import com.qz.sns.model.dto.PageDTO;
import com.qz.sns.model.dto.UserViewHistoryResponseDTO;
import com.qz.sns.model.dto.ViewHistoryQueryDTO;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.impl.ViewHistoryServiceImpl;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * <p>
 * 用户浏览历史表 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
/**
 * 用户浏览历史控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user/view-history")
@RequiredArgsConstructor
@Validated
public class ViewHistoryController {
    @Autowired
    private final ViewHistoryServiceImpl userViewHistoryService;

    /**
     * 添加浏览记录
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Void> addViewHistory(
            @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @RequestParam @NotNull(message = "内容ID不能为空") Long contentId) {

        log.info("接收添加浏览记录请求，用户ID：{}，内容ID：{}", userId, contentId);

        try {
            userViewHistoryService.addViewHistory(userId, contentId);
            return ResultUtils.success("添加浏览记录成功");
        } catch (Exception e) {
            log.error("添加浏览记录失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "添加浏览记录失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询用户浏览历史
     *
     * @param userId 用户ID
     * @param current 页码
     * @param size 每页大小
     * @param contentType 内容类型
     * @param categoryId 分类ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页浏览历史列表
     */
    @GetMapping("/{userId}")
    public Result<PageDTO<UserViewHistoryResponseDTO>> getUserViewHistory(
            @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        log.info("接收查询用户浏览历史请求，用户ID：{}，页码：{}，大小：{}", userId, current, size);

        try {
            ViewHistoryQueryDTO queryDTO = new ViewHistoryQueryDTO();
            queryDTO.setUserId(userId);
            queryDTO.setCurrent(current);
            queryDTO.setSize(size);
            queryDTO.setContentType(contentType);
            queryDTO.setCategoryId(categoryId);
            queryDTO.setStartTime(startTime);
            queryDTO.setEndTime(endTime);

            PageDTO<UserViewHistoryResponseDTO> pageResult = userViewHistoryService.getUserViewHistory(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("查询用户浏览历史失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询浏览历史失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除浏览历史
     *
     * @param userId 用户ID
     * @param ids 浏览记录ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{userId}/batch")
    public Result<Void> batchDeleteViewHistory(
            @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @RequestBody @NotEmpty(message = "浏览记录ID列表不能为空") Long[] ids) {

        log.info("接收批量删除浏览历史请求，用户ID：{}，记录数量：{}", userId, ids.length);

        try {
            boolean success = userViewHistoryService.batchDeleteViewHistory(userId, ids);

            if (success) {
                return ResultUtils.success("批量删除成功");
            } else {
                return ResultUtils.error(500, "批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除浏览历史失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "批量删除浏览历史失败：" + e.getMessage());
        }
    }

    /**
     * 清空用户所有浏览历史
     *
     * @param userId 用户ID
     * @return 清空结果
     */
    @DeleteMapping("/{userId}/all")
    public Result<Void> clearAllViewHistory(@PathVariable @NotNull(message = "用户ID不能为空") Long userId) {
        log.info("接收清空用户浏览历史请求，用户ID：{}", userId);

        try {
            boolean success = userViewHistoryService.clearAllViewHistory(userId);

            if (success) {
                return ResultUtils.success("清空浏览历史成功");
            } else {
                return ResultUtils.error(500, "清空浏览历史失败");
            }
        } catch (Exception e) {
            log.error("清空用户浏览历史失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "清空浏览历史失败：" + e.getMessage());
        }
    }
}