package com.qz.sns.sv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qz.sns.model.entity.FamilyRelation;
import com.qz.sns.model.entity.User;
import com.qz.sns.model.vo.FamilyIntimacyAnalysisVO;
import com.qz.sns.sv.mapper.FamilyRelationMapper;
import com.qz.sns.sv.mapper.GroupMessageMapper;
import com.qz.sns.sv.mapper.PrivateMessageMapper;
import com.qz.sns.sv.mapper.UserBehaviorMapper;
import com.qz.sns.sv.service.IFamilyIntimacyService;
import com.qz.sns.sv.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.*;
import java.util.Collections;

/**
 * 家庭关系亲密度分析服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FamilyIntimacyServiceImpl implements IFamilyIntimacyService {

    private final FamilyRelationMapper familyRelationMapper;
    private final PrivateMessageMapper privateMessageMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final IUserService userService;

    // 权重配置
    private static final double MESSAGE_WEIGHT = 0.4;      // 消息互动权重
    private static final double READING_WEIGHT = 0.3;       // 阅读互动权重
    private static final double INTERACTION_WEIGHT = 0.2;  // 行为互动权重
    private static final double BASE_WEIGHT = 0.1;          // 关系基础分权重

    // 时间衰减权重
    private static final double RECENT_WEEK_WEIGHT = 1.0;   // 最近7天
    private static final double MID_WEEK_WEIGHT = 0.7;      // 8-14天
    private static final double OLD_WEEK_WEIGHT = 0.4;       // 15-30天

    // 消息类型权重
    private static final Map<Integer, Double> MESSAGE_TYPE_WEIGHTS;
    static {
        Map<Integer, Double> map = new HashMap<>();
        map.put(0, 1.0);   // 文字
        map.put(1, 1.5);   // 图片
        map.put(2, 2.0);   // 文件
        map.put(3, 2.5);   // 语音
        map.put(4, 3.0);   // 视频
        MESSAGE_TYPE_WEIGHTS = Collections.unmodifiableMap(map);
    }

    // 关系类型基础分
    private static final Map<String, Double> RELATION_BASE_SCORES;
    static {
        Map<String, Double> map = new HashMap<>();
        // 父母子女关系
        map.put("父子", 15.0);
        map.put("父女", 15.0);
        map.put("母子", 15.0);
        map.put("母女", 15.0);
        map.put("子父", 15.0);
        map.put("子母", 15.0);
        map.put("女父", 15.0);
        map.put("女母", 15.0);
        // 兄弟姐妹关系
        map.put("兄弟", 10.0);
        map.put("兄妹", 10.0);
        map.put("姐弟", 10.0);
        map.put("姐妹", 10.0);
        // 祖孙关系
        map.put("祖孙", 12.0);
        map.put("奶孙", 12.0);
        // 其他关系
        map.put("其他亲属", 5.0);
        RELATION_BASE_SCORES = Collections.unmodifiableMap(map);
    }

    @Override
    public FamilyIntimacyAnalysisVO calculateIntimacyAnalysis(Long userId) {
        log.info("开始计算用户ID为{}的家庭关系亲密度分析", userId);

        try {
            // 1. 获取所有家庭成员（使用OR条件查询双向关系）
            LambdaQueryWrapper<FamilyRelation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.and(wrapper -> 
                wrapper.eq(FamilyRelation::getUserId, userId).or().eq(FamilyRelation::getRelativeId, userId)
            );
            List<FamilyRelation> allRelations = familyRelationMapper.selectList(queryWrapper);
            
            log.info("用户ID：{}的家庭关系记录数：{}", userId, allRelations.size());
            
            // 2. 处理所有家庭成员，去重
            Set<Long> processedRelativeIds = new HashSet<>();
            List<FamilyIntimacyAnalysisVO.MemberIntimacyVO> members = new ArrayList<>();
            
            for (FamilyRelation relation : allRelations) {
                // 确定对方用户ID
                Long relativeId = relation.getUserId().equals(userId) 
                    ? relation.getRelativeId() 
                    : relation.getUserId();
                
                if (processedRelativeIds.contains(relativeId)) continue;
                processedRelativeIds.add(relativeId);
                
                User relative = userService.getUserById(relativeId);
                if (relative == null) {
                    log.warn("未找到家庭成员用户，ID：{}", relativeId);
                    continue;
                }
                
                // 解析关系类型：格式为 "关系1,关系2"
                String relationTypeStr = relation.getRelationType();
                String[] relationTypes = relationTypeStr.split(",");
                
                // 确定当前用户的关系类型
                String currentUserRelationType;
                if (relation.getUserId().equals(userId)) {
                    // 当前用户是user_id，使用第一个关系类型
                    currentUserRelationType = relationTypes.length > 0 ? relationTypes[0] : relationTypeStr;
                } else {
                    // 当前用户是relative_id，使用第二个关系类型（如果存在）
                    currentUserRelationType = relationTypes.length > 1 ? relationTypes[1] : relationTypes[0];
                }
                
                FamilyIntimacyAnalysisVO.MemberIntimacyVO member = calculateMemberIntimacy(
                    userId, relativeId, relative, currentUserRelationType
                );
                members.add(member);
            }
            
            // 2. 计算百分比（以最高分为100%）
            if (!members.isEmpty()) {
                double maxScore = members.stream()
                    .mapToDouble(m -> m.getIntimacyScore() != null ? m.getIntimacyScore() : 0.0)
                    .max()
                    .orElse(1.0);
                
                if (maxScore > 0) {
                    for (FamilyIntimacyAnalysisVO.MemberIntimacyVO member : members) {
                        double percentage = (member.getIntimacyScore() / maxScore) * 100.0;
                        member.setIntimacyPercentage(Math.min(100.0, Math.max(0.0, percentage)));
                    }
                } else {
                    // 如果所有分数都是0，设置默认百分比
                    for (FamilyIntimacyAnalysisVO.MemberIntimacyVO member : members) {
                        member.setIntimacyPercentage(50.0); // 默认50%
                    }
                }
            }
            
            // 3. 计算总体亲密度（平均值）
            double overallIntimacy = members.stream()
                .mapToDouble(m -> m.getIntimacyPercentage() != null ? m.getIntimacyPercentage() : 0.0)
                .average()
                .orElse(0.0);
            
            // 4. 计算本周活跃度排行（包括当前用户和所有家庭成员）
            List<FamilyIntimacyAnalysisVO.ActivityRankingVO> activityRanking = calculateActivityRanking(userId, processedRelativeIds);
            
            // 5. 构建返回结果
            FamilyIntimacyAnalysisVO result = new FamilyIntimacyAnalysisVO();
            result.setOverallIntimacy(overallIntimacy);
            result.setMembers(members);
            result.setActivityRanking(activityRanking);
            
            log.info("计算完成，用户ID：{}，家庭成员数：{}，总体亲密度：{}，活跃度排行数：{}", 
                userId, members.size(), overallIntimacy, activityRanking.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("计算家庭关系亲密度分析失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new RuntimeException("计算亲密度分析失败：" + e.getMessage());
        }
    }

    /**
     * 计算与单个成员之间的亲密度
     */
    private FamilyIntimacyAnalysisVO.MemberIntimacyVO calculateMemberIntimacy(
            Long userId, Long relativeId, User relative, String relationType) {
        
        // 计算时间范围（最近30天）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime fourteenDaysAgo = now.minusDays(14);
        
        // 1. 计算消息互动分数
        double messageScore = calculateMessageScore(userId, relativeId, 
            sevenDaysAgo, fourteenDaysAgo, thirtyDaysAgo, now);
        
        // 2. 计算阅读互动分数
        double readingScore = calculateReadingScore(userId, relativeId, thirtyDaysAgo);
        
        // 3. 计算行为互动分数
        double interactionScore = calculateInteractionScore(userId, relativeId, thirtyDaysAgo);
        
        // 4. 获取关系基础分
        double baseScore = getRelationBaseScore(relationType);
        
        // 5. 计算总分数
        double totalScore = messageScore * MESSAGE_WEIGHT +
                           readingScore * READING_WEIGHT +
                           interactionScore * INTERACTION_WEIGHT +
                           baseScore * BASE_WEIGHT;
        
        // 6. 获取统计数据
        long privateMessageCount = privateMessageMapper.countPrivateMessagesInPeriod(
            userId, relativeId, thirtyDaysAgo, now);
        long sharedReadingCount = userBehaviorMapper.countSharedReading(
            userId, relativeId, thirtyDaysAgo);
        long interactionCount = userBehaviorMapper.countInteractions(
            userId, relativeId, thirtyDaysAgo);
        
        // 群聊消息数（需要获取共同群组）
        long groupMessageCount = calculateGroupMessageCount(userId, relativeId, thirtyDaysAgo, now);
        
        // 7. 构建成员亲密度VO
        FamilyIntimacyAnalysisVO.MemberIntimacyVO member = new FamilyIntimacyAnalysisVO.MemberIntimacyVO();
        member.setId(relative.getId());
        member.setNickname(relative.getNickname());
        member.setUsername(relative.getUsername());
        member.setAvatar(relative.getAvatar() != null ? relative.getAvatar() : relative.getAvatarThumb());
        member.setRelationType(relationType);
        member.setIntimacyScore(totalScore);
        
        // 8. 设置详细分数
        FamilyIntimacyAnalysisVO.IntimacyDetails details = new FamilyIntimacyAnalysisVO.IntimacyDetails();
        details.setMessageScore(messageScore);
        details.setReadingScore(readingScore);
        details.setInteractionScore(interactionScore);
        details.setBaseScore(baseScore);
        member.setDetails(details);
        
        // 9. 设置统计数据
        FamilyIntimacyAnalysisVO.IntimacyStats stats = new FamilyIntimacyAnalysisVO.IntimacyStats();
        stats.setPrivateMessageCount(privateMessageCount);
        stats.setGroupMessageCount(groupMessageCount);
        stats.setSharedReadingCount(sharedReadingCount);
        stats.setInteractionCount(interactionCount);
        member.setStats(stats);
        
        return member;
    }

    /**
     * 计算消息互动分数
     */
    private double calculateMessageScore(Long userId, Long relativeId,
                                        LocalDateTime sevenDaysAgo, LocalDateTime fourteenDaysAgo,
                                        LocalDateTime thirtyDaysAgo, LocalDateTime now) {
        double score = 0.0;
        
        // 1. 私聊消息分数
        // 最近7天
        List<Map<String, Object>> recentPrivateMessages = privateMessageMapper.countPrivateMessagesByType(
            userId, relativeId, sevenDaysAgo, now);
        double recentPrivateScore = calculateScoreByMessageTypes(recentPrivateMessages, RECENT_WEEK_WEIGHT);
        
        // 8-14天
        List<Map<String, Object>> midPrivateMessages = privateMessageMapper.countPrivateMessagesByType(
            userId, relativeId, fourteenDaysAgo, sevenDaysAgo);
        double midPrivateScore = calculateScoreByMessageTypes(midPrivateMessages, MID_WEEK_WEIGHT);
        
        // 15-30天
        List<Map<String, Object>> oldPrivateMessages = privateMessageMapper.countPrivateMessagesByType(
            userId, relativeId, thirtyDaysAgo, fourteenDaysAgo);
        double oldPrivateScore = calculateScoreByMessageTypes(oldPrivateMessages, OLD_WEEK_WEIGHT);
        
        double privateScore = recentPrivateScore + midPrivateScore + oldPrivateScore;
        
        // 检查双向互动（双方都有发送消息）
        long userToRelative = privateMessageMapper.countPrivateMessagesInPeriod(
            userId, relativeId, thirtyDaysAgo, now);
        long relativeToUser = privateMessageMapper.countPrivateMessagesInPeriod(
            relativeId, userId, thirtyDaysAgo, now);
        
        if (userToRelative > 0 && relativeToUser > 0) {
            privateScore *= 1.2; // 双向互动加分20%
        }
        
        // 2. 群聊消息分数（简化处理，使用共同群组的平均活跃度）
        double groupScore = calculateGroupMessageScore(userId, relativeId, 
            sevenDaysAgo, fourteenDaysAgo, thirtyDaysAgo, now);
        
        // 3. 综合分数：私聊60%，群聊40%
        score = privateScore * 0.6 + groupScore * 0.4;
        
        return score;
    }

    /**
     * 计算群聊消息分数
     */
    private double calculateGroupMessageScore(Long userId, Long relativeId,
                                             LocalDateTime sevenDaysAgo, LocalDateTime fourteenDaysAgo,
                                             LocalDateTime thirtyDaysAgo, LocalDateTime now) {
        // 获取用户所在的群组ID（假设第一个群组是家庭群组）
        Long userGroupId = familyRelationMapper.getUserFamilyGroupId(userId);
        Long relativeGroupId = familyRelationMapper.getUserFamilyGroupId(relativeId);
        
        if (userGroupId == null || relativeGroupId == null || !userGroupId.equals(relativeGroupId)) {
            return 0.0;
        }
        
        // 计算用户在群组中的消息数
        List<Map<String, Object>> userRecentMsgs = groupMessageMapper.countGroupMessagesByType(
            userGroupId, userId, sevenDaysAgo, now);
        List<Map<String, Object>> userMidMsgs = groupMessageMapper.countGroupMessagesByType(
            userGroupId, userId, fourteenDaysAgo, sevenDaysAgo);
        List<Map<String, Object>> userOldMsgs = groupMessageMapper.countGroupMessagesByType(
            userGroupId, userId, thirtyDaysAgo, fourteenDaysAgo);
        
        double userScore = calculateScoreByMessageTypes(userRecentMsgs, RECENT_WEEK_WEIGHT) +
                          calculateScoreByMessageTypes(userMidMsgs, MID_WEEK_WEIGHT) +
                          calculateScoreByMessageTypes(userOldMsgs, OLD_WEEK_WEIGHT);
        
        // 计算对方在群组中的消息数
        List<Map<String, Object>> relativeRecentMsgs = groupMessageMapper.countGroupMessagesByType(
            userGroupId, relativeId, sevenDaysAgo, now);
        List<Map<String, Object>> relativeMidMsgs = groupMessageMapper.countGroupMessagesByType(
            userGroupId, relativeId, fourteenDaysAgo, sevenDaysAgo);
        List<Map<String, Object>> relativeOldMsgs = groupMessageMapper.countGroupMessagesByType(
            userGroupId, relativeId, thirtyDaysAgo, fourteenDaysAgo);
        
        double relativeScore = calculateScoreByMessageTypes(relativeRecentMsgs, RECENT_WEEK_WEIGHT) +
                               calculateScoreByMessageTypes(relativeMidMsgs, MID_WEEK_WEIGHT) +
                               calculateScoreByMessageTypes(relativeOldMsgs, OLD_WEEK_WEIGHT);
        
        // 使用几何平均数（双方都活跃时分数更高）
        return Math.sqrt(userScore * relativeScore);
    }

    /**
     * 计算群聊消息数量
     */
    private long calculateGroupMessageCount(Long userId, Long relativeId,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        Long userGroupId = familyRelationMapper.getUserFamilyGroupId(userId);
        Long relativeGroupId = familyRelationMapper.getUserFamilyGroupId(relativeId);
        
        if (userGroupId == null || relativeGroupId == null || !userGroupId.equals(relativeGroupId)) {
            return 0;
        }
        
        return groupMessageMapper.countGroupMessagesInPeriod(userGroupId, userId, startTime, endTime) +
               groupMessageMapper.countGroupMessagesInPeriod(userGroupId, relativeId, startTime, endTime);
    }

    /**
     * 根据消息类型计算分数
     */
    private double calculateScoreByMessageTypes(List<Map<String, Object>> messageCounts, double periodWeight) {
        double score = 0.0;
        
        if (messageCounts == null || messageCounts.isEmpty()) {
            return score;
        }
        
        for (Map<String, Object> entry : messageCounts) {
            try {
                int type = Integer.parseInt(String.valueOf(entry.get("type")));
                long count = Long.parseLong(String.valueOf(entry.get("count")));
                
                Double typeWeight = MESSAGE_TYPE_WEIGHTS.getOrDefault(type, 1.0);
                score += count * typeWeight;
            } catch (Exception e) {
                log.warn("解析消息类型或数量失败：{}", e.getMessage());
            }
        }
        
        return score * periodWeight;
    }

    /**
     * 计算阅读互动分数
     */
    private double calculateReadingScore(Long userId, Long relativeId, LocalDateTime startTime) {
        // 1. 共同阅读内容数（基础分：每篇5分）
        Long sharedReadingCount = userBehaviorMapper.countSharedReading(userId, relativeId, startTime);
        double baseScore = sharedReadingCount * 5.0;
        
        // 2. 同一天阅读相同内容（加分：每篇3分）
        Long sameDayReadingCount = userBehaviorMapper.countSameDayReading(userId, relativeId, startTime);
        double sameDayBonus = sameDayReadingCount * 3.0;
        
        return baseScore + sameDayBonus;
    }

    /**
     * 计算行为互动分数
     */
    private double calculateInteractionScore(Long userId, Long relativeId, LocalDateTime startTime) {
        // 获取互动行为数（点赞、评论、分享）
        Long interactionCount = userBehaviorMapper.countInteractions(userId, relativeId, startTime);
        
        // 简化处理：每次互动平均3分
        // 实际可以根据互动类型细分（点赞2分，评论5分，分享3分）
        return interactionCount * 3.0;
    }

    /**
     * 获取关系基础分
     */
    private double getRelationBaseScore(String relationType) {
        return RELATION_BASE_SCORES.getOrDefault(relationType, 5.0);
    }
    
    /**
     * 计算本周活跃度排行
     * 包括当前用户和所有家庭成员
     */
    private List<FamilyIntimacyAnalysisVO.ActivityRankingVO> calculateActivityRanking(
            Long userId, Set<Long> familyMemberIds) {
        
        log.info("开始计算活跃度排行，用户ID：{}，家庭成员数：{}", userId, familyMemberIds.size());
        
        // 计算本周时间范围（从周一开始）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.with(DayOfWeek.MONDAY)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (weekStart.isAfter(now)) {
            weekStart = weekStart.minusWeeks(1);
        }
        LocalDateTime weekEnd = weekStart.plusWeeks(1);
        
        List<FamilyIntimacyAnalysisVO.ActivityRankingVO> ranking = new ArrayList<>();
        
        // 1. 添加当前用户
        User currentUser = userService.getUserById(userId);
        if (currentUser != null) {
            FamilyIntimacyAnalysisVO.ActivityRankingVO currentUserRanking = 
                calculateMemberActivity(userId, weekStart, weekEnd);
            if (currentUserRanking != null) {
                ranking.add(currentUserRanking);
            }
        }
        
        // 2. 添加所有家庭成员
        for (Long memberId : familyMemberIds) {
            FamilyIntimacyAnalysisVO.ActivityRankingVO memberRanking = 
                calculateMemberActivity(memberId, weekStart, weekEnd);
            if (memberRanking != null) {
                ranking.add(memberRanking);
            }
        }
        
        // 3. 按活跃度分数降序排序
        ranking.sort((a, b) -> Integer.compare(
            b.getActivityScore() != null ? b.getActivityScore() : 0,
            a.getActivityScore() != null ? a.getActivityScore() : 0
        ));
        
        log.info("活跃度排行计算完成，共{}人", ranking.size());
        
        return ranking;
    }
    
    /**
     * 计算单个成员的活跃度
     */
    private FamilyIntimacyAnalysisVO.ActivityRankingVO calculateMemberActivity(
            Long memberId, LocalDateTime weekStart, LocalDateTime weekEnd) {
        
        try {
            User member = userService.getUserById(memberId);
            if (member == null) {
                return null;
            }
            
            // 1. 计算群聊消息数（本周）
            Long chatMessages = 0L;
            Long familyGroupId = familyRelationMapper.getUserFamilyGroupId(memberId);
            if (familyGroupId != null) {
                chatMessages = (long) groupMessageMapper.countGroupMessagesInPeriod(
                    familyGroupId, memberId, weekStart, weekEnd);
            }
            
            // 2. 计算互动次数（本周）
            // 直接统计用户本周的所有互动行为（点赞、评论、分享）
            Long interactions = userBehaviorMapper.countUserInteractionsInPeriod(
                memberId, weekStart, weekEnd);
            if (interactions == null) {
                interactions = 0L;
            }
            
            // 3. 计算活跃度分数（0-100）
            // 群聊消息：每条0.5分，最高50分
            // 互动次数：每次2分，最高50分
            double chatScore = Math.min(50.0, chatMessages * 0.5);
            double interactionScore = Math.min(50.0, interactions * 2.0);
            int activityScore = (int) Math.round(chatScore + interactionScore);
            activityScore = Math.min(100, Math.max(0, activityScore));
            
            // 4. 构建返回对象
            FamilyIntimacyAnalysisVO.ActivityRankingVO ranking = 
                new FamilyIntimacyAnalysisVO.ActivityRankingVO();
            ranking.setId(member.getId());
            ranking.setNickname(member.getNickname());
            ranking.setAvatar(member.getAvatar() != null ? member.getAvatar() : member.getAvatarThumb());
            ranking.setChatMessages(chatMessages);
            ranking.setInteractions(interactions);
            ranking.setActivityScore(activityScore);
            
            return ranking;
            
        } catch (Exception e) {
            log.error("计算成员活跃度失败，成员ID：{}，错误：{}", memberId, e.getMessage(), e);
            return null;
        }
    }
}

