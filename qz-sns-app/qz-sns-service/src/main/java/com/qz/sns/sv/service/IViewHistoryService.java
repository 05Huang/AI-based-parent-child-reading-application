package com.qz.sns.sv.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qz.sns.model.dto.PageDTO;
import com.qz.sns.model.dto.UserViewHistoryResponseDTO;
import com.qz.sns.model.dto.ViewHistoryQueryDTO;
import com.qz.sns.model.entity.ViewHistory;

/**
 * <p>
 * 用户浏览历史表 服务类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
public interface IViewHistoryService extends IService<ViewHistory> {
    /**
     * 添加浏览记录
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     */
    void addViewHistory(Long userId, Long contentId);

    /**
     * 分页查询用户浏览历史
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageDTO<UserViewHistoryResponseDTO> getUserViewHistory(ViewHistoryQueryDTO queryDTO);

    /**
     * 批量删除浏览历史
     *
     * @param userId 用户ID
     * @param ids 浏览记录ID列表
     * @return 删除结果
     */
    boolean batchDeleteViewHistory(Long userId, Long[] ids);

    /**
     * 清空用户所有浏览历史
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean clearAllViewHistory(Long userId);
}
