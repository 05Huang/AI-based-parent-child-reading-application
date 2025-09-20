package com.qz.sns.web.controller;

import com.qz.sns.model.dto.FavoriteQueryDTO;
import com.qz.sns.model.dto.PageDTO;
import com.qz.sns.model.dto.UserFavoriteResponseDTO;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.impl.FavoriteServiceImpl;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * <p>
 * 用户收藏表 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */

/**
 * 用户收藏管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user/favorite")
@RequiredArgsConstructor
@Validated
public class FavoriteController {

    @Resource
    private FavoriteServiceImpl userFavoriteService;

    /**
     * 添加收藏记录
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param note 收藏备注
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> addFavorite(
            @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @RequestParam @NotNull(message = "内容ID不能为空") Long contentId,
            @RequestParam(required = false) String note) {

        log.info("接收添加收藏请求，用户ID：{}，内容ID：{}，备注：{}", userId, contentId, note);

        try {
            userFavoriteService.addFavorite(userId, contentId, note);
            return ResultUtils.success("收藏成功");
        } catch (Exception e) {
            log.error("添加收藏失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "添加收藏失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户收藏内容
     *
     * @param queryDTO 查询条件
     * @return 分页收藏内容列表
     */
    @GetMapping
    public Result<PageDTO<UserFavoriteResponseDTO>> getUserFavorites(@Valid FavoriteQueryDTO queryDTO) {
        log.info("接收查询用户收藏内容请求，查询条件：{}", queryDTO);

        try {
            PageDTO<UserFavoriteResponseDTO> pageResult = userFavoriteService.getUserFavorites(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("查询用户收藏内容失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询收藏内容失败：" + e.getMessage());
        }
    }

    /**
     * 删除收藏记录
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 操作结果
     */
    @DeleteMapping
    public Result<Void> deleteFavorite(
            @RequestParam @NotNull(message = "用户ID不能为空") Long userId,
            @RequestParam @NotNull(message = "内容ID不能为空") Long contentId) {

        log.info("接收删除收藏请求，用户ID：{}，内容ID：{}", userId, contentId);

        try {
            boolean success = userFavoriteService.deleteFavorite(userId, contentId);
            return success ? ResultUtils.success("删除成功") : ResultUtils.error(500, "删除失败");
        } catch (Exception e) {
            log.error("删除收藏失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "删除收藏失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除用户收藏记录
     *
     * @param userId 用户ID
     * @param ids 收藏记录ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{userId}/batch")
    public Result<Void> batchDeleteFavorites(
            @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @RequestBody @NotEmpty(message = "收藏记录ID列表不能为空") Long[] ids) {

        log.info("接收批量删除收藏请求，用户ID：{}，记录数量：{}", userId, ids.length);

        try {
            boolean success = userFavoriteService.batchDeleteFavorites(userId, ids);
            return success ? ResultUtils.success("批量删除成功") : ResultUtils.error(500, "批量删除失败");
        } catch (Exception e) {
            log.error("批量删除收藏失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "批量删除收藏失败：" + e.getMessage());
        }
    }
}