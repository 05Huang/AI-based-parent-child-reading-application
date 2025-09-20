package com.qz.sns.sv.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qz.sns.model.dto.LikeDTO;
import com.qz.sns.model.entity.LikeRecord;

import java.util.Map;

/**
 * <p>
 * 点赞记录表 服务类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
public interface ILikeRecordService extends IService<LikeRecord> {
    /**
     * 点赞/取消点赞
     * @param likeDTO 点赞DTO
     * @return 点赞结果，包含isLiked和likeCount
     */
    Map<String, Object> like(LikeDTO likeDTO);

    /**
     * 获取点赞状态
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 类型(1:内容 2:评论)
     * @return 是否已点赞
     */
    Boolean getLikeStatus(Long userId, Long targetId, Integer type);

}
