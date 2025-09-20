package com.qz.sns.sv.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.dto.CategoryQueryDTO;
import com.qz.sns.model.entity.ContentCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 内容分类表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Repository
public interface ContentCategoryMapper extends BaseMapper<ContentCategory> {
    /**
     * 分页查询分类列表
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<ContentCategory> selectCategoryPage(Page<ContentCategory> page, @Param("query") CategoryQueryDTO queryDTO);

    /**
     * 检查分类名称是否已存在
     *
     * @param name 分类名称
     * @param excludeId 排除的分类ID（用于更新时检查）
     * @return 存在的数量
     */
    int checkNameExists(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 获取所有正常状态的分类（按排序字段排序）
     *
     * @return 分类列表
     */
    List<ContentCategory> selectAllActiveCategories();

}
