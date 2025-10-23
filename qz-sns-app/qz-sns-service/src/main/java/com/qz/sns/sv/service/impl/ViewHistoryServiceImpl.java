package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.dto.PageDTO;
import com.qz.sns.model.dto.UserViewHistoryResponseDTO;
import com.qz.sns.model.dto.ViewHistoryQueryDTO;
import com.qz.sns.model.entity.ViewHistory;
import com.qz.sns.sv.mapper.ViewHistoryMapper;
import com.qz.sns.sv.service.IViewHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户浏览历史表 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@Service
public class ViewHistoryServiceImpl extends ServiceImpl<ViewHistoryMapper, ViewHistory> implements IViewHistoryService {

    @Autowired
    private ViewHistoryMapper userViewHistoryMapper;

    /**
     * 添加浏览记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addViewHistory(Long userId, Long contentId) {
        log.info("添加浏览记录，用户ID：{}，内容ID：{}", userId, contentId);

        if (userId == null || contentId == null) {
            log.warn("用户ID或内容ID为空，跳过添加浏览记录");
            return;
        }

        try {
            // 检查是否已经存在浏览记录
            Long existingId = userViewHistoryMapper.checkUserViewHistory(userId, contentId);

            if (existingId != null) {
                // 如果已存在，更新浏览时间
                int updated = userViewHistoryMapper.updateViewTime(userId, contentId);
                log.info("更新浏览时间，用户ID：{}，内容ID：{}，影响行数：{}", userId, contentId, updated);
            } else {
                // 如果不存在，插入新的浏览记录
                ViewHistory viewHistory = new ViewHistory();
                viewHistory.setUserId(userId);
                viewHistory.setContentId(contentId);
                viewHistory.setViewTime(LocalDateTime.now());

                int inserted = userViewHistoryMapper.insert(viewHistory);
                log.info("新增浏览记录，用户ID：{}，内容ID：{}，影响行数：{}", userId, contentId, inserted);
            }
        } catch (Exception e) {
            log.error("添加浏览记录失败，用户ID：{}，内容ID：{}，错误：{}", userId, contentId, e.getMessage(), e);
            // 浏览记录添加失败不应该影响主业务，所以这里只记录日志，不抛出异常
        }
    }

    /**
     * 分页查询用户浏览历史
     */
    @Override
    public PageDTO<UserViewHistoryResponseDTO> getUserViewHistory(ViewHistoryQueryDTO queryDTO) {
        log.info("分页查询用户浏览历史，查询条件：{}", queryDTO);

        // 构建分页对象
        Page<UserViewHistoryResponseDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 执行分页查询
        IPage<UserViewHistoryResponseDTO> historyPage = userViewHistoryMapper.selectViewHistoryPage(page, queryDTO);

        // 构建返回结果
        PageDTO<UserViewHistoryResponseDTO> result = new PageDTO<>();
        result.setRecords(historyPage.getRecords());
        result.setTotal(historyPage.getTotal());
        result.setCurrent((int) historyPage.getCurrent());
        result.setSize((int) historyPage.getSize());
        result.setPages(historyPage.getPages());

        log.info("用户浏览历史查询完成，用户ID：{}，共查询到{}条记录，总页数：{}", queryDTO.getUserId(), result.getTotal(), result.getPages());
        return result;
    }

    /**
     * 批量删除浏览历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteViewHistory(Long userId, Long[] ids) {
        log.info("批量删除浏览历史，用户ID：{}，记录数量：{}", userId, ids.length);

        if (userId == null || ids == null || ids.length == 0) {
            throw new IllegalArgumentException("用户ID或浏览记录ID列表不能为空");
        }

        try {
            int deletedCount = userViewHistoryMapper.batchDeleteByUserAndIds(userId, ids);
            boolean success = deletedCount > 0;

            log.info("批量删除浏览历史完成，用户ID：{}，删除数量：{}", userId, deletedCount);
            return success;
        } catch (Exception e) {
            log.error("批量删除浏览历史失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new RuntimeException("批量删除浏览历史失败：" + e.getMessage());
        }
    }

    /**
     * 清空用户所有浏览历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearAllViewHistory(Long userId) {
        log.info("清空用户所有浏览历史，用户ID：{}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        try {
            int deletedCount = userViewHistoryMapper.deleteAllByUser(userId);
            boolean success = deletedCount > 0;

            log.info("清空用户浏览历史完成，用户ID：{}，删除数量：{}", userId, deletedCount);
            return success;
        } catch (Exception e) {
            log.error("清空用户浏览历史失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new RuntimeException("清空用户浏览历史失败：" + e.getMessage());
        }
    }
}
