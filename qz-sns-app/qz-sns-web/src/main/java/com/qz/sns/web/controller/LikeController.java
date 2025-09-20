package com.qz.sns.web.controller;

import com.qz.sns.model.dto.LikeDTO;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.impl.LikeRecordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * <p>
 * 点赞记录表 前端控制器
 * </p>
 *
 * @author 李彬
 * @since 2025-02-18
 */
@RestController
@RequestMapping("/api/like")
public class LikeController {

    @Autowired
    private LikeRecordServiceImpl likeService;

    /**
     * 点赞/取消点赞
     * @param like 点赞信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Object> like(@RequestBody LikeDTO like) {
        Object result = likeService.like(like);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户的点赞状态
     * @param userId 用户ID
     * @param targetId 目标ID (内容或评论ID)
     * @param type 类型 (1:内容，2:评论)
     * @return 是否已点赞
     */
    @GetMapping("/status")
    public Result<Boolean> getLikeStatus(
            @RequestParam Long userId,
            @RequestParam Long targetId,
            @RequestParam Integer type) {
        Boolean status = likeService.getLikeStatus(userId, targetId, type);
        return ResultUtils.success(status);
    }
}
