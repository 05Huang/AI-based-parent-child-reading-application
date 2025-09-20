package com.qz.sns.web.controller;

import com.qz.sns.common.enums.UserType;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.ContentCategory;
import com.qz.sns.sv.annotation.RequirePermission;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.impl.ContentCategoryServiceImpl;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Target;
import java.util.List;

/**
 * 内容分类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Validated
public class ContentCategoryController {

    @Resource
    private ContentCategoryServiceImpl contentCategoryService;

    /**
     * 创建分类
     *
     * @param createDTO 分类创建请求数据
     * @return 创建成功的分类信息
     */
    @RequirePermission(value = UserType.ADMIN)
    @PostMapping
    public Result<ContentCategory> createCategory(@Valid @RequestBody CategoryCreateDTO createDTO) {
        log.info("接收创建分类请求：{}", createDTO.getName());

        try {
            ContentCategory category = contentCategoryService.createCategory(createDTO);
            return ResultUtils.success(category, "分类创建成功");
        } catch (Exception e) {
            log.error("创建分类失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "创建分类失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类详细信息
     */
    @GetMapping("/{id}")
    public Result<ContentCategory> getCategoryById(@PathVariable @NotNull(message = "分类ID不能为空") Long id) {
        log.info("接收获取分类详情请求，分类ID：{}", id);

        try {
            ContentCategory category = contentCategoryService.getCategoryById(id);
            return ResultUtils.success(category);
        } catch (Exception e) {
            log.error("获取分类详情失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取分类详情失败：" + e.getMessage());
        }
    }

    /**
     * 更新分类信息
     *
     * @param updateDTO 分类更新请求数据
     * @return 更新后的分类信息
     */
    @PutMapping
    public Result<ContentCategory> updateCategory(@Valid @RequestBody CategoryUpdateDTO updateDTO) {
        log.info("接收更新分类请求，分类ID：{}", updateDTO.getId());

        try {
            ContentCategory category = contentCategoryService.updateCategory(updateDTO);
            return ResultUtils.success(category, "分类信息更新成功");
        } catch (Exception e) {
            log.error("更新分类信息失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "更新分类信息失败：" + e.getMessage());
        }
    }

    /**
     * 删除分类（逻辑删除）
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @RequirePermission(value = UserType.ADMIN)
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable @NotNull(message = "分类ID不能为空") Long id) {
        log.info("接收删除分类请求，分类ID：{}", id);

        try {
            boolean success = contentCategoryService.deleteCategory(id);

            if (success) {
                return ResultUtils.success("分类删除成功");
            } else {
                return ResultUtils.error(500, "分类删除失败");
            }
        } catch (Exception e) {
            log.error("删除分类失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "删除分类失败：" + e.getMessage());
        }
    }

    /**
     * 更新分类状态
     *
     * @param id 分类ID
     * @param status 状态值：1-正常，0-禁用
     * @return 操作结果
     */
    @RequirePermission(value = UserType.ADMIN)
    @PatchMapping("/{id}/status")
    public Result<Void> updateCategoryStatus(
            @PathVariable @NotNull(message = "分类ID不能为空") Long id,
            @RequestParam @NotNull(message = "状态值不能为空") Integer status) {

        log.info("接收更新分类状态请求，分类ID：{}，状态：{}", id, status);

        try {
            boolean success = contentCategoryService.updateCategoryStatus(id, status);

            if (success) {
                String statusText = status == 1 ? "启用" : "禁用";
                return ResultUtils.success("分类" + statusText + "成功");
            } else {
                return ResultUtils.error(500, "分类状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新分类状态失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "更新分类状态失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询分类列表
     * 支持通过status参数查询不同状态的分类：
     * - status=1: 查询正常分类（默认）
     * - status=0: 查询禁用分类
     * - 不传status: 默认查询正常分类
     *
     * @param queryDTO 查询条件和分页参数
     * @return 分页分类列表
     */
    @GetMapping("/page")
    public Result<PageDTO<ContentCategory>> getCategoryPage(@Valid CategoryQueryDTO queryDTO) {
        log.info("接收分页查询分类请求，查询条件：{}", queryDTO);

        try {
            PageDTO<ContentCategory> pageResult = contentCategoryService.getCategoryPage(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("分页查询分类失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询分类列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有正常状态的分类
     *
     * @return 分类列表
     */
    @GetMapping("/all")
    public Result<List<ContentCategory>> getAllActiveCategories() {
        log.info("接收获取所有正常状态分类请求");

        try {
            List<ContentCategory> categories = contentCategoryService.getAllActiveCategories();
            return ResultUtils.success(categories);
        } catch (Exception e) {
            log.error("获取所有正常状态分类失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "获取分类列表失败：" + e.getMessage());
        }
    }

    /**
     * 检查分类名称是否可用
     *
     * @param name 分类名称
     * @param excludeId 排除的分类ID（可选，用于更新时检查）
     * @return 检查结果
     */
    @GetMapping("/check/name")
    public Result<Boolean> checkCategoryNameAvailable(
            @RequestParam String name,
            @RequestParam(required = false) Long excludeId) {

        log.info("检查分类名称可用性，分类名称：{}，排除ID：{}", name, excludeId);

        try {
            boolean exists = contentCategoryService.isCategoryNameExists(name, excludeId);
            return ResultUtils.success(!exists, exists ? "分类名称已存在" : "分类名称可用");
        } catch (Exception e) {
            log.error("检查分类名称可用性失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "检查分类名称失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除分类
     *
     * @param ids 分类ID列表
     * @return 批量删除结果
     */
    @DeleteMapping("/batch")
    public Result<BatchDeleteResult> batchDeleteCategory(@RequestBody @NotEmpty(message = "分类ID列表不能为空") Long[] ids) {
        log.info("接收批量删除分类请求，分类ID数量：{}", ids.length);

        try {
            BatchDeleteResult result = contentCategoryService.batchDeleteCategory(ids);

            if (result.isAllSuccess()) {
                return ResultUtils.success(result, "批量删除成功");
            } else {
                return ResultUtils.success(result,
                        String.format("批量删除完成，成功：%d，失败：%d",
                                result.getSuccessCount(), result.getFailCount()));
            }
        } catch (Exception e) {
            log.error("批量删除分类失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "批量删除分类失败：" + e.getMessage());
        }
    }
/**
 * 批量更新分类状态
 *
 * @param ids 分类ID列表
 * @param status 目标状态
 * @return 批量更新结果
 */
@PatchMapping("/batch/status")
public Result<BatchUpdateResult> batchUpdateCategoryStatus(
        @RequestBody @NotEmpty(message = "分类ID列表不能为空") Long[] ids,
        @RequestParam @NotNull(message = "状态值不能为空") Integer status) {

    log.info("接收批量更新分类状态请求，分类ID数量：{}，状态：{}", ids.length, status);

    try {
        BatchUpdateResult result = contentCategoryService.batchUpdateCategoryStatus(ids, status);

        String statusText = status == 1 ? "启用" : "禁用";
        if (result.isAllSuccess()) {
            return ResultUtils.success(result, "批量" + statusText + "成功");
        } else {
            return ResultUtils.success(result,
                    String.format("批量%s完成，成功：%d，失败：%d",
                            statusText, result.getSuccessCount(), result.getFailCount()));
        }
    } catch (Exception e) {
        log.error("批量更新分类状态失败：{}", e.getMessage(), e);
        return ResultUtils.error(500, "批量更新分类状态失败：" + e.getMessage());
    }
}

    /**
     * 便捷的禁用分类查询接口
     *
     * @param current 页码
     * @param size 每页大小
     * @param name 分类名称关键词
     * @return 禁用分类列表
     */
    @GetMapping("/disabled")
    public Result<PageDTO<ContentCategory>> getDisabledCategories(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {

        log.info("接收禁用分类查询请求，页码：{}，大小：{}，名称：{}", current, size, name);

        try {
            CategoryQueryDTO queryDTO = new CategoryQueryDTO();
            queryDTO.setCurrent(current);
            queryDTO.setSize(size);
            queryDTO.setStatus(0); // 查询禁用状态
            queryDTO.setName(name);

            PageDTO<ContentCategory> pageResult = contentCategoryService.getCategoryPage(queryDTO);
            return ResultUtils.success(pageResult);
        } catch (Exception e) {
            log.error("查询禁用分类失败：{}", e.getMessage(), e);
            return ResultUtils.error(500, "查询禁用分类失败：" + e.getMessage());
        }
    }
}