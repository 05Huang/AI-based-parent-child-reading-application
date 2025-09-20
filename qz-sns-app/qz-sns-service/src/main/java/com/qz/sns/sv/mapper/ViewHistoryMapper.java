package com.qz.sns.sv.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.dto.UserViewHistoryResponseDTO;
import com.qz.sns.model.dto.ViewHistoryQueryDTO;
import com.qz.sns.model.entity.ViewHistory;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 用户浏览历史表 Mapper 接口
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Repository
public interface ViewHistoryMapper extends BaseMapper<ViewHistory> {
    /**
     * 分页查询用户浏览历史（包含内容信息）
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<UserViewHistoryResponseDTO> selectViewHistoryPage(Page<UserViewHistoryResponseDTO> page,
                                                            @Param("query") ViewHistoryQueryDTO queryDTO);

    /**
     * 检查用户是否已经浏览过某个内容
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 浏览记录ID，如果没有则返回null
     */
    Long checkUserViewHistory(@Param("userId") Long userId, @Param("contentId") Long contentId);

    /**
     * 更新浏览时间（如果用户重复浏览同一内容）
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 影响行数
     */
    int updateViewTime(@Param("userId") Long userId, @Param("contentId") Long contentId);

    /**
     * 批量删除用户浏览历史
     *
     * @param userId 用户ID
     * @param ids 浏览记录ID列表
     * @return 影响行数
     */
    int batchDeleteByUserAndIds(@Param("userId") Long userId, @Param("ids") Long[] ids);

    /**
     * 清空用户所有浏览历史
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteAllByUser(@Param("userId") Long userId);
}
