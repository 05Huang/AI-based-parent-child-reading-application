package com.qz.sns.sv.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.ContentCategory;

import java.util.List;

/**
 * <p>
 * 内容分类表 服务类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
public interface IContentCategoryService extends IService<ContentCategory> {
    /**
     * 创建分类
     *
     * @param createDTO 创建请求数据
     * @return 创建成功的分类信息
     */
    ContentCategory createCategory(CategoryCreateDTO createDTO);

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类详细信息
     */
    ContentCategory getCategoryById(Long id);

    /**
     * 更新分类信息
     *
     * @param updateDTO 更新请求数据
     * @return 更新后的分类信息
     */
    ContentCategory updateCategory(CategoryUpdateDTO updateDTO);

    /**
     * 删除分类（逻辑删除）
     *
     * @param id 分类ID
     * @return 删除是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 更新分类状态
     *
     * @param id 分类ID
     * @param status 状态值：1-正常，0-禁用
     * @return 操作是否成功
     */
    boolean updateCategoryStatus(Long id, Integer status);

    /**
     * 分页查询分类列表
     *
     * @param queryDTO 查询条件和分页参数
     * @return 分页结果
     */
    PageDTO<ContentCategory> getCategoryPage(CategoryQueryDTO queryDTO);

    /**
     * 获取所有正常状态的分类
     *
     * @return 分类列表
     */
    List<ContentCategory> getAllActiveCategories();

    /**
     * 检查分类名称是否已存在
     *
     * @param name 分类名称
     * @param excludeId 排除的分类ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isCategoryNameExists(String name, Long excludeId);

    /**
     * 批量删除分类
     *
     * @param ids 分类ID列表
     * @return 批量删除结果
     */
    BatchDeleteResult batchDeleteCategory(Long[] ids);

    /**
     * 批量更新分类状态
     *
     * @param ids 分类ID列表
     * @param status 目标状态
     * @return 批量更新结果
     */
    BatchUpdateResult batchUpdateCategoryStatus(Long[] ids, Integer status);

}
