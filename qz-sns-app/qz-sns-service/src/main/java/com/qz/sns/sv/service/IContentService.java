package com.qz.sns.sv.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qz.sns.model.dto.*;
import com.qz.sns.model.entity.Content;
import com.qz.sns.model.vo.ContentResponse;

import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * <p>
 * 内容表 服务类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
public interface IContentService extends IService<Content> {
    /**
     * 创建内容
     * @param request 内容请求
     * @param creatorId 创建者ID
     * @return 创建的内容
     */
    ContentResponse createContent(ContentDTO request, Long creatorId);

    /**
     * 更新内容
     * @param id 内容ID
     * @param request 内容请求
     * @param userId 用户ID
     * @return 更新后的内容
     */
    ContentResponse updateContent(Long id, ContentDTO request, Long userId);

    /**
     * 获取内容详情
     * @param id 内容ID
     * @return 内容详情DTO
     */
    ContentRequest getContentDetail(Long id);

    /**
     * 增加浏览量
     * @param id 内容ID
     */
    void incrementViewCount(Long id);

    /**
     * 分页查询内容列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageDTO<ContentRequest> getContentPage(ContentQueryDTO queryDTO);

    /**
     * 删除内容（逻辑删除，同时删除相关评论）
     *
     * @param deleteDTO 删除请求
     * @return 删除结果
     */
    boolean deleteContent(ContentDeleteDTO deleteDTO);

    /**
     * 批量删除内容（逻辑删除，同时删除相关评论）
     *
     * @param batchDeleteDTO 批量删除请求
     * @return 删除结果统计
     */
    BatchDeleteResult batchDeleteContent(ContentBatchDeleteDTO batchDeleteDTO);

    /**
     * 搜索内容
     *
     * @param keyword 搜索关键词
     * @param current 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    PageDTO<ContentRequest> searchContent(String keyword, Integer current, Integer size);

    /**
     * 获取热门内容
     *
     * @param limit 限制数量
     * @return 热门内容列表
     */
    List<ContentRequest> getHotContent(Integer limit);

    /**
     * 根据分类获取内容
     *
     * @param categoryId 分类ID
     * @param limit 限制数量
     * @return 内容列表
     */
    List<ContentRequest> getContentByCategory(Long categoryId, Integer limit);

    /**
     * 根据创建者获取内容
     *
     * @param creatorId 创建者ID
     * @param current 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageDTO<ContentRequest> getContentByCreator(Long creatorId, Integer current, Integer size);

    /**
     * 根据标签获取内容
     *
     * @param tag 标签
     * @param current 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageDTO<ContentRequest> getContentByTag(String tag, Integer current, Integer size);

    /**
     * 更新内容状态
     *
     * @param id 内容ID
     * @param status 状态
     * @return 更新结果
     */
    boolean updateContentStatus(Long id, Integer status);
}
