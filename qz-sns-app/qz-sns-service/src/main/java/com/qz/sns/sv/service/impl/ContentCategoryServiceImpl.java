package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.common.utils.BeanUtils;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.ContentCategory;
import com.qz.sns.sv.mapper.ContentCategoryMapper;
import com.qz.sns.sv.service.IContentCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 内容分类表 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@Service
public class ContentCategoryServiceImpl extends ServiceImpl<ContentCategoryMapper, ContentCategory> implements IContentCategoryService {
    @Autowired
    private  ContentCategoryMapper contentCategoryMapper;

    /**
     * 创建分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentCategory createCategory(CategoryCreateDTO createDTO) {
        log.info("开始创建分类，分类名称：{}", createDTO.getName());

        // 验证分类名称唯一性
        if (isCategoryNameExists(createDTO.getName(), null)) {
            throw new RuntimeException("分类名称已存在");
        }

        // 构建分类实体
        ContentCategory category = new ContentCategory();
        BeanUtils.copyProperties(createDTO, category);
        category.setStatus(1); // 默认正常状态

        // 保存到数据库
        int result = contentCategoryMapper.insert(category);
        if (result <= 0) {
            throw new RuntimeException("分类创建失败");
        }

        log.info("分类创建成功，分类ID：{}", category.getId());
        return category;
    }

    /**
     * 根据ID获取分类详情
     */
    @Override
    public ContentCategory getCategoryById(Long id) {
        log.info("查询分类详情，分类ID：{}", id);

        if (id == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }

        QueryWrapper<ContentCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("status", 1); // 只查询正常状态的分类

        ContentCategory category = contentCategoryMapper.selectOne(queryWrapper);
        if (category == null) {
            throw new RuntimeException("分类不存在或已被禁用");
        }

        return category;
    }

    /**
     * 更新分类信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentCategory updateCategory(CategoryUpdateDTO updateDTO) {
        log.info("开始更新分类信息，分类ID：{}", updateDTO.getId());

        // 验证分类是否存在且未被禁用
        ContentCategory existingCategory = getCategoryById(updateDTO.getId());
        if (existingCategory == null) {
            throw new RuntimeException("分类不存在或已被禁用");
        }

        // 验证分类名称唯一性（如果要更新分类名称）
        if (!updateDTO.getName().equals(existingCategory.getName()) &&
                isCategoryNameExists(updateDTO.getName(), updateDTO.getId())) {
            throw new RuntimeException("分类名称已存在");
        }

        // 构建更新实体
        ContentCategory updateCategory = new ContentCategory();
        BeanUtils.copyProperties(updateDTO, updateCategory);

        // 执行更新操作
        UpdateWrapper<ContentCategory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", updateDTO.getId())
                .eq("status", 1); // 确保只更新正常状态的分类

        int result = contentCategoryMapper.update(updateCategory, updateWrapper);
        if (result <= 0) {
            throw new RuntimeException("分类更新失败");
        }

        log.info("分类更新成功，分类ID：{}", updateDTO.getId());

        // 返回更新后的分类信息
        return getCategoryById(updateDTO.getId());
    }

    /**
     * 删除分类（逻辑删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        log.info("开始删除分类，分类ID：{}", id);

        if (id == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }

        // 验证分类是否存在
        ContentCategory category = getCategoryById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在或已被删除");
        }

        // 执行逻辑删除
        UpdateWrapper<ContentCategory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .eq("status", 1)
                .set("status", 0);

        int result = contentCategoryMapper.update(null, updateWrapper);
        boolean success = result > 0;

        if (success) {
            log.info("分类删除成功，分类ID：{}", id);
        } else {
            log.error("分类删除失败，分类ID：{}", id);
        }

        return success;
    }

    /**
     * 更新分类状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategoryStatus(Long id, Integer status) {
        log.info("更新分类状态，分类ID：{}，目标状态：{}", id, status);

        if (id == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }

        // 验证分类是否存在（不过滤状态）
        QueryWrapper<ContentCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        ContentCategory category = contentCategoryMapper.selectOne(queryWrapper);

        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        // 检查状态是否需要更新
        if (category.getStatus().equals(status)) {
            log.info("分类状态无需更新，分类ID：{}，当前状态：{}", id, status);
            return true;
        }

        // 更新状态
        UpdateWrapper<ContentCategory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("status", status);

        int result = contentCategoryMapper.update(null, updateWrapper);
        boolean success = result > 0;

        if (success) {
            String statusText = status == 1 ? "启用" : "禁用";
            log.info("分类状态更新成功，分类ID：{}，状态：{}", id, statusText);
        }

        return success;
    }

    /**
     * 分页查询分类列表
     */
    @Override
    public PageDTO<ContentCategory> getCategoryPage(CategoryQueryDTO queryDTO) {
        log.info("分页查询分类列表，查询条件：{}", queryDTO);

        // 构建分页对象
        Page<ContentCategory> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 执行分页查询
        IPage<ContentCategory> categoryPage = contentCategoryMapper.selectCategoryPage(page, queryDTO);

        // 构建返回结果
        PageDTO<ContentCategory> result = new PageDTO<>();
        result.setRecords(categoryPage.getRecords());
        result.setTotal(categoryPage.getTotal());
        result.setCurrent((int) categoryPage.getCurrent());
        result.setSize((int) categoryPage.getSize());

        log.info("分类列表查询完成，共查询到{}条记录", result.getTotal());
        return result;
    }

    /**
     * 获取所有正常状态的分类
     */
    @Override
    public List<ContentCategory> getAllActiveCategories() {
        log.info("获取所有正常状态的分类");

        List<ContentCategory> categories = contentCategoryMapper.selectAllActiveCategories();
        log.info("获取正常状态分类完成，共{}条记录", categories.size());

        return categories;
    }

    /**
     * 检查分类名称是否已存在
     */
    @Override
    public boolean isCategoryNameExists(String name, Long excludeId) {
        if (!StringUtils.hasText(name)) {
            return false;
        }

        int count = contentCategoryMapper.checkNameExists(name, excludeId);
        return count > 0;
    }

    /**
     * 批量删除分类
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchDeleteResult batchDeleteCategory(Long[] ids) {
        log.info("开始批量删除分类，分类数量：{}", ids.length);

        BatchDeleteResult result = new BatchDeleteResult();
        int successCount = 0;
        int failCount = 0;

        for (Long id : ids) {
            try {
                boolean success = deleteCategory(id);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("删除分类失败，分类ID：{}，错误：{}", id, e.getMessage());
                failCount++;
            }
        }

        log.info("批量删除分类完成，成功：{}，失败：{}", successCount, failCount);

        result.setTotalCount(ids.length);
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setErrorMessage(failCount > 0 ? "部分分类删除失败" : null);

        return result;
    }

    /**
     * 批量更新分类状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchUpdateResult batchUpdateCategoryStatus(Long[] ids, Integer status) {
        log.info("批量更新分类状态，分类数量：{}，目标状态：{}", ids.length, status);

        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException("分类ID列表不能为空");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }

        BatchUpdateResult result = new BatchUpdateResult();
        int successCount = 0;
        int failCount = 0;

        for (Long id : ids) {
            try {
                boolean success = updateCategoryStatus(id, status);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("更新分类状态失败，分类ID：{}，错误：{}", id, e.getMessage());
                failCount++;
            }
        }

        String statusText = status == 1 ? "启用" : "禁用";
        log.info("批量{}分类完成，成功：{}，失败：{}", statusText, successCount, failCount);

        result.setTotalCount(ids.length);
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setErrorMessage(failCount > 0 ? "部分分类状态更新失败" : null);

        return result;
    }
}
