package com.qz.sns.sv.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.dto.ContentQueryDTO;
import com.qz.sns.model.dto.ContentRequest;
import com.qz.sns.model.entity.Content;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 内容表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Repository
public interface ContentMapper extends BaseMapper<Content> {

    Content selectById(Long id);

    @Update("UPDATE content SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Update("UPDATE content SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);

    @Update("UPDATE content SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decrementLikeCount(Long id);

    @Update("UPDATE content SET comment_count = comment_count + 1 WHERE id = #{id}")
    void incrementCommentCount(Long id);

    /**
     * 分页查询内容列表（包含创建者信息）
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<ContentRequest> selectContentPage(Page<ContentRequest> page, @Param("query") ContentQueryDTO queryDTO);

    /**
     * 根据ID获取内容详情（包含创建者信息）
     *
     * @param id 内容ID
     * @return 内容详情
     */
    ContentRequest selectContentById(@Param("id") Long id);

    /**
     * 获取热门内容列表
     *
     * @param limit 限制数量
     * @return 热门内容列表
     */
    List<ContentRequest> selectHotContent(@Param("limit") Integer limit);

    /**
     * 根据分类获取内容列表
     *
     * @param categoryId 分类ID
     * @param limit 限制数量
     * @return 内容列表
     */
    List<ContentRequest> selectContentByCategory(@Param("categoryId") Long categoryId, @Param("limit") Integer limit);

    /**
     * 根据创建者获取内容列表
     *
     * @param creatorId 创建者ID
     * @param page 分页参数
     * @return 分页结果
     */
    IPage<ContentRequest> selectContentByCreator(Page<ContentRequest> page, @Param("creatorId") Long creatorId);

    /**
     * 搜索内容（标题和内容模糊匹配）
     *
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param type 内容类型（可选）
     * @return 分页结果
     */
    IPage<ContentRequest> searchContent(Page<ContentRequest> page, @Param("keyword") String keyword, @Param("type") Integer type);

    /**
     * 根据标签获取内容列表
     *
     * @param page 分页参数
     * @param tag 标签
     * @return 分页结果
     */
    IPage<ContentRequest> selectContentByTag(Page<ContentRequest> page, @Param("tag") String tag);

    /**
            * 根据ID获取内容详情（不过滤状态）
            *
            * @param id 内容ID
 * @return 内容详情
 */
    ContentRequest selectContentByIdWithoutStatusFilter(@Param("id") Long id);

}
