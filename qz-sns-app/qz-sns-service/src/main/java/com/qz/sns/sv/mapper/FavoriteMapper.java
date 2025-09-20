package com.qz.sns.sv.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.dto.FavoriteQueryDTO;
import com.qz.sns.model.dto.UserFavoriteResponseDTO;
import com.qz.sns.model.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 用户收藏表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Repository
public interface FavoriteMapper extends BaseMapper<Favorite> {
    /**
     * 分页查询用户收藏内容列表（包含内容信息）
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<UserFavoriteResponseDTO> selectFavoritePage(Page<UserFavoriteResponseDTO> page,
                                                      @Param("query") FavoriteQueryDTO queryDTO);

    /**
     * 检查用户是否已经收藏某个内容
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 收藏记录ID，如果没有则返回null
     */
    Long checkUserFavorite(@Param("userId") Long userId, @Param("contentId") Long contentId);
    /**
     * 批量删除用户收藏记录
     *
     * @param userId 用户ID
     * @param ids 收藏记录ID列表
     * @return 影响行数
     */
    int batchDeleteByUserAndIds(@Param("userId") Long userId, @Param("ids") Long[] ids);
}
