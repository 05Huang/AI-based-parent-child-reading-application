package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.dto.FavoriteQueryDTO;
import com.qz.sns.model.dto.PageDTO;
import com.qz.sns.model.dto.UserBehaviorDTO;
import com.qz.sns.model.dto.UserFavoriteResponseDTO;
import com.qz.sns.model.entity.Favorite;
import com.qz.sns.sv.mapper.FavoriteMapper;
import com.qz.sns.sv.service.IFavoriteService;
import com.qz.sns.sv.service.UserBehaviorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户收藏表 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Slf4j
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {

    @Resource
    private FavoriteMapper userFavoriteMapper;
    
    @Autowired
    private UserBehaviorService userBehaviorService;
    /**
     * 添加收藏记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long userId, Long contentId, String note) {
        log.info("添加收藏记录，用户ID：{}，内容ID：{}，备注：{}", userId, contentId, note);

        if (userId == null || contentId == null) {
            log.warn("用户ID或内容ID为空，跳过添加收藏记录");
            return;
        }

        // 检查用户是否已经收藏过该内容
        Long existingId = userFavoriteMapper.checkUserFavorite(userId, contentId);

        if (existingId != null) {
            log.warn("用户ID：{} 已经收藏过内容ID：{}，更新备注", userId, contentId);
            // 如果已存在，更新备注
            Favorite favorite = new Favorite();
            favorite.setId(existingId);
            favorite.setNote(note);
            userFavoriteMapper.updateById(favorite);
        } else {
            // 如果不存在，插入新的收藏记录
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setContentId(contentId);
            favorite.setNote(note);
            favorite.setCreatedTime(LocalDateTime.now());
            favorite.setStatus(1); // 默认正常状态

            userFavoriteMapper.insert(favorite);
            log.info("新增收藏记录成功，用户ID：{}，内容ID：{}", userId, contentId);
        }
    }

    /**
     * 查询用户收藏内容
     */
    @Override
    public PageDTO<UserFavoriteResponseDTO> getUserFavorites(FavoriteQueryDTO queryDTO) {
        log.info("查询用户收藏内容，查询条件：{}", queryDTO);

        // 构建分页对象
        Page<UserFavoriteResponseDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 执行分页查询
        IPage<UserFavoriteResponseDTO> favoritePage = userFavoriteMapper.selectFavoritePage(page, queryDTO);

        // 构建返回结果
        PageDTO<UserFavoriteResponseDTO> result = new PageDTO<>();
        result.setRecords(favoritePage.getRecords());
        result.setTotal(favoritePage.getTotal());
        result.setCurrent((int) favoritePage.getCurrent());
        result.setSize((int) favoritePage.getSize());

        log.info("用户收藏内容查询完成，共查询到{}条记录", result.getTotal());
        return result;
    }

    /**
     * 删除收藏记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFavorite(Long userId, Long contentId) {
        log.info("删除收藏记录，用户ID：{}，内容ID：{}", userId, contentId);

        // 验证收藏记录是否存在
        Long existingId = userFavoriteMapper.checkUserFavorite(userId, contentId);

        if (existingId == null) {
            log.warn("用户ID：{} 没有收藏内容ID：{}，无法删除", userId, contentId);
            return false;
        }

        // 执行删除操作
        int result = userFavoriteMapper.deleteById(existingId);
        boolean success = result > 0;

        if (success) {
            log.info("删除收藏记录成功，用户ID：{}，内容ID：{}", userId, contentId);
        } else {
            log.error("删除收藏记录失败，用户ID：{}，内容ID：{}", userId, contentId);
        }

        return success;
    }

    /**
     * 批量删除用户收藏记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteFavorites(Long userId, Long[] ids) {
        log.info("批量删除用户收藏记录，用户ID：{}，记录数量：{}", userId, ids.length);

        if (userId == null || ids == null || ids.length == 0) {
            throw new IllegalArgumentException("用户ID或收藏记录ID列表不能为空");
        }

        // 调用Mapper方法批量删除
        int deletedCount = userFavoriteMapper.batchDeleteByUserAndIds(userId, ids);
        boolean success = deletedCount > 0;

        log.info("批量删除收藏记录完成，用户ID：{}，删除数量：{}", userId, deletedCount);
        return success;
    }

    /**
     * 获取用户对某个内容的收藏状态
     */
    @Override
    public boolean getFavoriteStatus(Long userId, Long contentId) {
        log.info("查询用户收藏状态，用户ID：{}，内容ID：{}", userId, contentId);

        if (userId == null || contentId == null) {
            log.warn("用户ID或内容ID为空，返回未收藏状态");
            return false;
        }

        // 检查用户是否已经收藏过该内容
        Long existingId = userFavoriteMapper.checkUserFavorite(userId, contentId);
        boolean isFavorited = existingId != null;

        log.info("用户收藏状态查询完成，用户ID：{}，内容ID：{}，收藏状态：{}", userId, contentId, isFavorited);
        return isFavorited;
    }

    /**
     * 切换收藏状态（收藏/取消收藏）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long userId, Long contentId) {
        log.info("切换收藏状态，用户ID：{}，内容ID：{}", userId, contentId);

        if (userId == null || contentId == null) {
            throw new IllegalArgumentException("用户ID或内容ID不能为空");
        }

        // 检查用户是否已经收藏过该内容
        Long existingId = userFavoriteMapper.checkUserFavorite(userId, contentId);

        if (existingId != null) {
            // 如果已收藏，则取消收藏
            int result = userFavoriteMapper.deleteById(existingId);
            boolean success = result > 0;
            
            if (success) {
                log.info("取消收藏成功，用户ID：{}，内容ID：{}", userId, contentId);
            } else {
                log.error("取消收藏失败，用户ID：{}，内容ID：{}", userId, contentId);
                throw new RuntimeException("取消收藏失败");
            }
            return false; // 返回false表示已取消收藏
        } else {
            // 如果未收藏，则添加收藏
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setContentId(contentId);
            favorite.setNote(""); // 默认空备注
            favorite.setCreatedTime(LocalDateTime.now());
            favorite.setStatus(1); // 默认正常状态

            int result = userFavoriteMapper.insert(favorite);
            boolean success = result > 0;
            
            if (success) {
                log.info("添加收藏成功，用户ID：{}，内容ID：{}", userId, contentId);
                
                // 记录用户收藏行为到user_behavior表
                try {
                    UserBehaviorDTO behaviorDTO = new UserBehaviorDTO();
                    behaviorDTO.setUserId(userId);
                    behaviorDTO.setContentId(contentId);
                    behaviorDTO.setBehaviorType("collect");
                    userBehaviorService.recordUserBehavior(behaviorDTO);
                    log.info("记录用户收藏行为成功，用户ID：{}，内容ID：{}", userId, contentId);
                } catch (Exception e) {
                    log.error("记录用户收藏行为失败", e);
                    // 不影响主流程
                }
            } else {
                log.error("添加收藏失败，用户ID：{}，内容ID：{}", userId, contentId);
                throw new RuntimeException("添加收藏失败");
            }
            return true; // 返回true表示已收藏
        }
    }
}
