package com.qz.sns.model.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 家庭关系亲密度分析VO
 */
@Data
public class FamilyIntimacyAnalysisVO {
    /**
     * 总体亲密度（所有成员的平均值）
     */
    private Double overallIntimacy;
    
    /**
     * 家庭成员亲密度列表
     */
    private List<MemberIntimacyVO> members;
    
    /**
     * 本周活跃度排行
     */
    private List<ActivityRankingVO> activityRanking;
    
    /**
     * 单个成员亲密度信息
     */
    @Data
    public static class MemberIntimacyVO {
        /**
         * 成员ID
         */
        private Long id;
        
        /**
         * 成员昵称
         */
        private String nickname;
        
        /**
         * 成员用户名
         */
        private String username;
        
        /**
         * 成员头像
         */
        private String avatar;
        
        /**
         * 关系类型（从当前用户视角）
         */
        private String relationType;
        
        /**
         * 亲密度分数
         */
        private Double intimacyScore;
        
        /**
         * 亲密度百分比（0-100）
         */
        private Double intimacyPercentage;
        
        /**
         * 详细分数分解
         */
        private IntimacyDetails details;
        
        /**
         * 统计数据
         */
        private IntimacyStats stats;
    }
    
    /**
     * 亲密度详细分数
     */
    @Data
    public static class IntimacyDetails {
        /**
         * 消息互动分数
         */
        private Double messageScore;
        
        /**
         * 阅读互动分数
         */
        private Double readingScore;
        
        /**
         * 行为互动分数
         */
        private Double interactionScore;
        
        /**
         * 关系基础分
         */
        private Double baseScore;
    }
    
    /**
     * 亲密度统计数据
     */
    @Data
    public static class IntimacyStats {
        /**
         * 私聊消息数（最近30天）
         */
        private Long privateMessageCount;
        
        /**
         * 群聊消息数（最近30天）
         */
        private Long groupMessageCount;
        
        /**
         * 共同阅读内容数
         */
        private Long sharedReadingCount;
        
        /**
         * 互动行为数（点赞、评论等）
         */
        private Long interactionCount;
    }
    
    /**
     * 本周活跃度排行VO
     */
    @Data
    public static class ActivityRankingVO {
        /**
         * 成员ID
         */
        private Long id;
        
        /**
         * 成员昵称
         */
        private String nickname;
        
        /**
         * 成员头像
         */
        private String avatar;
        
        /**
         * 群聊消息数（本周）
         */
        private Long chatMessages;
        
        /**
         * 互动次数（本周）
         */
        private Long interactions;
        
        /**
         * 活跃度分数（0-100）
         */
        private Integer activityScore;
    }
}

