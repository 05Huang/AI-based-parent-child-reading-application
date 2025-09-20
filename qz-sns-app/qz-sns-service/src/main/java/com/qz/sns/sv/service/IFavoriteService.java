package com.qz.sns.sv.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qz.sns.model.dto.FavoriteQueryDTO;
import com.qz.sns.model.dto.PageDTO;
import com.qz.sns.model.dto.UserFavoriteResponseDTO;
import com.qz.sns.model.entity.Favorite;

/**
 * <p>
 * 用户收藏表 服务类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
public interface IFavoriteService extends IService<Favorite> {
    /**
     * 添加收藏记录
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @param note 收藏备注
     */
    void addFavorite(Long userId, Long contentId, String note);

    /**
     * 查询用户收藏内容（带分页）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageDTO<UserFavoriteResponseDTO> getUserFavorites(FavoriteQueryDTO queryDTO);

    /**
     * 删除收藏记录
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 删除是否成功
     */
    boolean deleteFavorite(Long userId, Long contentId);

    /**
     * 批量删除用户收藏记录
     *
     * @param userId 用户ID
     * @param ids 收藏记录ID列表
     * @return 删除结果
     */
    boolean batchDeleteFavorites(Long userId, Long[] ids);
}
