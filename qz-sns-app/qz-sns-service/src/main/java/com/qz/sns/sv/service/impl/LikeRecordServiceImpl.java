package com.qz.sns.sv.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.dto.LikeDTO;
import com.qz.sns.model.entity.Comment;
import com.qz.sns.model.entity.Content;
import com.qz.sns.model.entity.LikeRecord;
import com.qz.sns.sv.mapper.CommentMapper;
import com.qz.sns.sv.mapper.ContentMapper;
import com.qz.sns.sv.mapper.LikeRecordMapper;
import com.qz.sns.sv.service.ILikeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 点赞记录表 服务实现类
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements ILikeRecordService {
    @Autowired
    private LikeRecordMapper likeRecordMapper;

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    @Transactional
    public Map<String, Object> like(LikeDTO likeDTO) {
        Long userId = likeDTO.getUserId();
        Long targetId = likeDTO.getTargetId();
        Integer type = likeDTO.getType();

        // 检查是否已点赞
        LikeRecord record = likeRecordMapper.selectByUserIdAndTarget(userId, targetId, type);

        Map<String, Object> result = new HashMap<>();
        if (record == null) {
            // 未点赞，添加点赞记录
            record = new LikeRecord();
            record.setUserId(userId);
            record.setTargetId(targetId);
            record.setType(type);
            likeRecordMapper.insert(record);

            // 增加目标点赞数
            if (type == 1) {
                // 内容点赞
                contentMapper.incrementLikeCount(targetId);
                Content content = contentMapper.selectById(targetId);
                result.put("likeCount", content.getLikeCount());
            } else if (type == 2) {
                // 评论点赞
                commentMapper.incrementLikeCount(targetId);
                Comment comment = commentMapper.selectById(targetId);
                result.put("likeCount", comment.getLikeCount());
            }

            result.put("isLiked", true);
        } else {
            // 已点赞，删除点赞记录
            likeRecordMapper.deleteById(record.getId());

            // 减少目标点赞数
            if (type == 1) {
                // 内容点赞
                contentMapper.decrementLikeCount(targetId);
                Content content = contentMapper.selectById(targetId);
                result.put("likeCount", content.getLikeCount());
            } else if (type == 2) {
                // 评论点赞
                commentMapper.decrementLikeCount(targetId);
                Comment comment = commentMapper.selectById(targetId);
                result.put("likeCount", comment.getLikeCount());
            }

            result.put("isLiked", false);
        }

        return result;
    }

    @Override
    public Boolean getLikeStatus(Long userId, Long targetId, Integer type) {
        LikeRecord record = likeRecordMapper.selectByUserIdAndTarget(userId, targetId, type);
        return record != null;
    }

}
