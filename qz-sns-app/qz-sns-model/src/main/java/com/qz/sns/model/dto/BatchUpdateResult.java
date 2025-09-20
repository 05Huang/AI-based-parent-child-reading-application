package com.qz.sns.model.dto;

import lombok.Data;

/**
 * 批量更新结果DTO
 */
@Data
public class BatchUpdateResult {
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failCount;
    
    /**
     * 影响的评论数量
     */
    private Integer affectedCommentCount;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return failCount == 0;
    }
    
    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }
}