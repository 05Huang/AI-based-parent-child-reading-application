package com.qz.sns.sv.service.impl;

import com.qz.sns.model.models.IntimacyScore;
import com.qz.sns.model.vo.IntimacyRankingVO;
import com.qz.sns.sv.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import com.qz.sns.common.constant.UserConstant;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntimacyService {

    private static final String GLOBAL_RANKING_KEY = "intimacy:global:ranking";
    private static final String FAMILY_RANKING_KEY_PREFIX = "intimacy:family:";
    private static final String USER_RANKING_KEY_PREFIX = "intimacy:user:";
    private static final String INTIMACY_HISTORY_KEY_PREFIX = "intimacy:history:";
    private static final String GLOBAL_USER_RANKS_KEY = "intimacy:global:user_ranks";
    private static final double BASE_SCORE_FOR_100 = 500.0;
    private static final int MAX_RANKING_SIZE = 50;

    // 私聊与群聊消息的权重比例
    private static final double PRIVATE_MSG_WEIGHT = 0.7;
    private static final double GROUP_MSG_WEIGHT = 0.3;

// 限制排行榜显示条数
    @Autowired
    private FamilyRelationMapper familyRelationMapper;

    @Autowired
    private GroupMessageMapper groupMessageMapper;

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private UserMapper userMapper;

    private final RedisTemplate<String, Object> redisTemplate;


    /**
     * 计算所有用户的亲密度并排名
     */
    public void calculateAndSaveAllIntimacyScores() {
        log.info("开始计算全网亲密度排名");
        long startTime = System.currentTimeMillis();

        // 获取所有家长用户ID（role=1）
        List<Long> parentIds = userMapper.getAllParentUserIds();
        // 获取所有拥有家庭关系的用户ID（防止某些家长role设置不正确但实际有家庭关系）
        List<Long> relationUserIds = familyRelationMapper.getAllUserIds();

        Set<Long> allUserIdsSet = new HashSet<>();
        if (parentIds != null) allUserIdsSet.addAll(parentIds);
        if (relationUserIds != null) allUserIdsSet.addAll(relationUserIds);

        // 排除测试用户（如阅读小助手）
        allUserIdsSet.remove(UserConstant.TEST_USER_ID);
        allUserIdsSet.remove(-999L); // 双重保险

        List<Long> allUserIds = new ArrayList<>(allUserIdsSet);
        log.info("获取到 {} 个用户ID参与亲密度计算（已去重并排除测试用户）", allUserIds.size());

        // 用于存储每个用户的亲密度分数
        Map<Long, List<IntimacyScore>> allUserScores = new HashMap<>();

        // 用于存储每个家庭群组的成员ID列表
        Map<Long, List<Long>> familyGroupMembers = new HashMap<>();

        // 存储每个用户的基本信息，确保相同ID使用相同的昵称和头像
        Map<Long, Map<String, Object>> userBasicInfoMap = new HashMap<>();

        // 计算每个用户的亲密度分数
        for (Long userId : allUserIds) {
            // 获取用户基本信息
            Map<String, Object> userInfo = userMapper.getUserBasicInfo(userId);
            if (userInfo != null) {
                userBasicInfoMap.put(userId, userInfo);
            }

            // 计算用户与其亲属的亲密度
            List<IntimacyScore> scores = calculateUserIntimacyScores(userId);
            allUserScores.put(userId, scores);

            // 获取用户所在的家庭群组ID
            Long familyGroupId = familyRelationMapper.getUserFamilyGroupId(userId);
            if (familyGroupId != null) {
                // 将用户添加到对应家庭群组的成员列表中
                familyGroupMembers.computeIfAbsent(familyGroupId, k -> new ArrayList<>()).add(userId);
                log.debug("用户 {} 归属于家庭群组 {}", userId, familyGroupId);
            } else {
                log.debug("用户 {} 未找到有效的家庭群组", userId);
            }
        }

        // 全网排名 - 每个用户只取其最高分的一条记录
        List<IntimacyScore> globalRankingFull = allUserIds.stream()
                .map(userId -> {
                    List<IntimacyScore> userScores = allUserScores.getOrDefault(userId, new ArrayList<>());
                    return userScores.isEmpty() ? null : userScores.get(0); // 取排名第一的亲密关系
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(IntimacyScore::getScore).reversed())
                .collect(Collectors.toList());

        // 计算所有人的百分比
        recalculatePercentages(globalRankingFull);

        // 保存所有用户的排名和百分比到Redis，用于查询个人排名
        Map<String, String> userRanksMap = new HashMap<>();
        for (int i = 0; i < globalRankingFull.size(); i++) {
            IntimacyScore score = globalRankingFull.get(i);
            // 格式: rank:percentage
            String value = (i + 1) + ":" + score.getPercentage();
            userRanksMap.put(String.valueOf(score.getUserId()), value);
        }
        redisTemplate.delete(GLOBAL_USER_RANKS_KEY);
        if (!userRanksMap.isEmpty()) {
            redisTemplate.opsForHash().putAll(GLOBAL_USER_RANKS_KEY, userRanksMap);
            redisTemplate.expire(GLOBAL_USER_RANKS_KEY, 7, TimeUnit.DAYS);
        }

        // 去重并限制前50名
        Set<Long> addedUserIds = new HashSet<>();
        List<IntimacyScore> globalRanking = new ArrayList<>();

        for (IntimacyScore score : globalRankingFull) {
            if (!addedUserIds.contains(score.getUserId())) {
                // 确保使用用户表中的昵称和头像，避免不一致
                Map<String, Object> userInfo = userBasicInfoMap.get(score.getUserId());
                if (userInfo != null) {
                    score.setNickname((String) userInfo.get("nickname"));
                    String avatar = (String) userInfo.get("avatar");
                    if (avatar == null || avatar.isEmpty()) {
                        avatar = (String) userInfo.get("avatar_thumb");
                    }
                    score.setAvatar(avatar);
                }

                globalRanking.add(score);
                addedUserIds.add(score.getUserId());

                if (globalRanking.size() >= MAX_RANKING_SIZE) {
                    break;
                }
            }
        }

        // 设置全局排名
        for (int i = 0; i < globalRanking.size(); i++) {
            globalRanking.get(i).setRank(i + 1);
        }

        // 修改：计算全网排名中的百分比，确保有区分度
        recalculatePercentages(globalRanking);

        // 对每个家庭群组也重新计算百分比
        for (Map.Entry<Long, List<Long>> entry : familyGroupMembers.entrySet()) {
            Long familyGroupId = entry.getKey();
            List<Long> memberIds = entry.getValue();

            // 收集每个成员的最高亲密度记录
            List<IntimacyScore> familyScores = new ArrayList<>();
            for (Long memberId : memberIds) {
                List<IntimacyScore> memberScores = allUserScores.getOrDefault(memberId, new ArrayList<>());
                if (!memberScores.isEmpty()) {
                    IntimacyScore bestScore = memberScores.get(0);

                    // 确保使用用户表中的昵称和头像
                    Map<String, Object> userInfo = userBasicInfoMap.get(memberId);
                    if (userInfo != null) {
                        bestScore.setNickname((String) userInfo.get("nickname"));
                        String avatar = (String) userInfo.get("avatar");
                        if (avatar == null || avatar.isEmpty()) {
                            avatar = (String) userInfo.get("avatar_thumb");
                        }
                        bestScore.setAvatar(avatar);
                    }

                    familyScores.add(bestScore);
                }
            }

            // 排序
            familyScores.sort(Comparator.comparing(IntimacyScore::getScore).reversed());

            // 重新计算家庭内排名百分比
            recalculatePercentages(familyScores);

            // 设置家庭内排名
            for (int i = 0; i < familyScores.size(); i++) {
                familyScores.get(i).setRank(i + 1);
            }

            // 更新到用户分数映射中
            for (IntimacyScore score : familyScores) {
                List<IntimacyScore> userScores = allUserScores.get(score.getUserId());
                if (userScores != null && !userScores.isEmpty()) {
                    userScores.set(0, score);
                }
            }
        }

        // 保存到Redis
        saveToRedis(globalRanking, allUserScores, familyGroupMembers, userBasicInfoMap);

        log.info("全网亲密度排名计算完成，耗时：{}ms", System.currentTimeMillis() - startTime);
    }

    /**
     * 重新计算百分比，使用渐变比例
     * 新增方法：确保排名中的亲密度百分比有区分度
     */


    /**
     * 计算单个用户的亲密度分数
     */
    private List<IntimacyScore> calculateUserIntimacyScores(Long userId) {
        // 原有代码保持不变...
        List<IntimacyScore> results = new ArrayList<>();

        // 获取用户的家庭关系
        List<Map<String, Object>> relations = familyRelationMapper.getRelationsWithUserInfo(userId);
        if (relations == null || relations.isEmpty()) {
            log.info("用户 {} 没有家庭关系记录，创建默认0分记录", userId);
            IntimacyScore selfScore = new IntimacyScore();
            selfScore.setUserId(userId);
            selfScore.setScore(0.0);
            selfScore.setPercentage(0.0);
            results.add(selfScore);
            return results;
        }

        // 获取用户的群聊列表
        List<Long> groupIds = groupMemberMapper.getUserGroupIds(userId);

        // 计算亲密度
        for (Map<String, Object> relation : relations) {
            Long relativeId = Long.valueOf(relation.get("relative_id").toString());
            String relationType = (String) relation.get("relation_type");
            String nickname = (String) relation.get("nickname");
            String avatar = (String) relation.get("avatar");
            String avatarThumb = (String) relation.get("avatar_thumb");

            // 计算私聊和群聊综合亲密度分数
            double privateScore = calculatePrivateIntimacyScore(userId, relativeId);
            double groupScore = calculateGroupIntimacyScore(userId, relativeId, groupIds);

            // 综合分数 = 私聊权重 * 私聊分数 + 群聊权重 * 群聊分数
            double finalScore = PRIVATE_MSG_WEIGHT * privateScore + GROUP_MSG_WEIGHT * groupScore;

            IntimacyScore intimacyScore = new IntimacyScore();
            intimacyScore.setUserId(userId);
            intimacyScore.setRelativeId(relativeId);
            intimacyScore.setRelationType(relationType);
            intimacyScore.setNickname(nickname);
            intimacyScore.setAvatar(avatar != null ? avatar : avatarThumb);
            intimacyScore.setScore(finalScore);

            results.add(intimacyScore);
        }

        // 排序并计算百分比
        results.sort(Comparator.comparing(IntimacyScore::getScore).reversed());

        double maxScore = results.isEmpty() ? 0 : results.get(0).getScore();
        for (IntimacyScore score : results) {
            // 百分比计算，保证最高分为100%
            double percentage = maxScore > 0 ? (score.getScore() / maxScore) * 100 : 0;
            score.setPercentage(Math.min(100.0, percentage)); // 确保不超过100%
        }

        return results;
    }

    /**
     * 计算私聊亲密度分数
     */
    private double calculatePrivateIntimacyScore(Long userId, Long relativeId) {
        // 计算时间范围 - 当前周和上一周
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekEnd = weekStart.plusDays(7);
        LocalDateTime lastWeekStart = weekStart.minusDays(7);

        // 计算私聊消息按类型统计的数量
        List<Map<String, Object>> currentWeekMsgTypes = privateMessageMapper.countPrivateMessagesByType(
                userId, relativeId, weekStart, weekEnd);

        List<Map<String, Object>> lastWeekMsgTypes = privateMessageMapper.countPrivateMessagesByType(
                userId, relativeId, lastWeekStart, weekStart);

        // 根据消息类型计算分数
        double currentWeekScore = calculateScoreByMessageTypes(currentWeekMsgTypes, 1.0);
        double lastWeekScore = calculateScoreByMessageTypes(lastWeekMsgTypes, 0.5); // 上周消息权重降低

        return currentWeekScore + lastWeekScore;
    }

    /**
     * 计算群聊亲密度分数
     */
    private double calculateGroupIntimacyScore(Long userId, Long relativeId, List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return 0.0;
        }

        // 计算时间范围 - 当前周和上一周
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekEnd = weekStart.plusDays(7);
        LocalDateTime lastWeekStart = weekStart.minusDays(7);

        double totalScore = 0.0;
        int commonGroupCount = 0;

        // 遍历所有群聊
        for (Long groupId : groupIds) {
            // 检查该亲属是否也在这个群中
            List<Long> memberIds = groupMemberMapper.getGroupMemberIds(groupId);
            if (!memberIds.contains(relativeId)) {
                continue; // 如果亲属不在该群中，跳过计算
            }

            commonGroupCount++;

            // 获取当前用户在该群的消息统计
            List<Map<String, Object>> currentUserMsgTypes = groupMessageMapper.countGroupMessagesByType(
                    groupId, userId, weekStart, weekEnd);
            List<Map<String, Object>> lastWeekUserMsgTypes = groupMessageMapper.countGroupMessagesByType(
                    groupId, userId, lastWeekStart, weekStart);

            // 获取目标用户在该群的消息统计
            List<Map<String, Object>> currentRelativeMsgTypes = groupMessageMapper.countGroupMessagesByType(
                    groupId, relativeId, weekStart, weekEnd);
            List<Map<String, Object>> lastWeekRelativeMsgTypes = groupMessageMapper.countGroupMessagesByType(
                    groupId, relativeId, lastWeekStart, weekStart);

            // 根据消息类型计算分数
            double currentUserScore = calculateScoreByMessageTypes(currentUserMsgTypes, 1.0);
            double lastWeekUserScore = calculateScoreByMessageTypes(lastWeekUserMsgTypes, 0.5);
            double currentRelativeScore = calculateScoreByMessageTypes(currentRelativeMsgTypes, 1.0);
            double lastWeekRelativeScore = calculateScoreByMessageTypes(lastWeekRelativeMsgTypes, 0.5);

            // 计算相互回复的分数（两人都活跃的情况下分数更高）
            double interactionScore = Math.sqrt(
                    (currentUserScore + lastWeekUserScore) * (currentRelativeScore + lastWeekRelativeScore)
            );

            totalScore += interactionScore;
        }

        // 返回所有共同群的平均分数，避免共同群数量多的用户得分偏高
        return commonGroupCount > 0 ? totalScore / commonGroupCount : 0;
    }

    /**
     * 根据消息类型计算分数
     */
    private double calculateScoreByMessageTypes(List<Map<String, Object>> messageCounts, double periodWeight) {
        double score = 0.0;

        if (messageCounts == null || messageCounts.isEmpty()) {
            return score;
        }

        // 不同类型消息的权重
        Map<Integer, Double> typeWeights = new HashMap<>();
        typeWeights.put(0, 1.0);    // 文字
        typeWeights.put(1, 1.5);    // 图片
        typeWeights.put(2, 2.0);    // 文件
        typeWeights.put(3, 2.5);    // 语音
        typeWeights.put(4, 3.0);    // 视频
        typeWeights.put(21, 0.5);   // 提示消息

        for (Map<String, Object> entry : messageCounts) {
            try {
                // 处理可能是Boolean或Integer的类型值
                int type;
                Object typeObj = entry.get("type");
                if (typeObj instanceof Boolean) {
                    type = ((Boolean) typeObj) ? 1 : 0;
                } else if (typeObj instanceof Integer) {
                    type = (Integer) typeObj;
                } else {
                    type = Integer.parseInt(String.valueOf(typeObj));
                }

                // 处理count值
                long count;
                Object countObj = entry.get("count");
                if (countObj instanceof Number) {
                    count = ((Number) countObj).longValue();
                } else {
                    count = Long.parseLong(String.valueOf(countObj));
                }

                // 根据类型计算得分
                Double typeWeight = typeWeights.getOrDefault(type, 1.0);
                score += count * typeWeight;
            } catch (Exception e) {
                log.warn("Invalid message type or count format: {}", e.getMessage());
                log.warn("Entry data: {}", entry);
                continue;
            }
        }

        return score * periodWeight;
    }

    /**
     * 保存结果到Redis
     */
    private void saveToRedis(List<IntimacyScore> globalRanking,
                             Map<Long, List<IntimacyScore>> allUserScores,
                             Map<Long, List<Long>> familyGroupMembers,
                             Map<Long, Map<String, Object>> userBasicInfoMap) {
        // 清除之前的缓存
        redisTemplate.delete(GLOBAL_RANKING_KEY);
        Set<String> userKeys = redisTemplate.keys(USER_RANKING_KEY_PREFIX + "*");
        if (userKeys != null && !userKeys.isEmpty()) {
            redisTemplate.delete(userKeys);
        }

        Set<String> familyKeys = redisTemplate.keys(FAMILY_RANKING_KEY_PREFIX + "*");
        if (familyKeys != null && !familyKeys.isEmpty()) {
            redisTemplate.delete(familyKeys);
        }

        // 保存全局排名（已确保去重）
        redisTemplate.opsForList().rightPushAll(GLOBAL_RANKING_KEY, globalRanking.toArray());

        // 保存用户个人排名
        for (Map.Entry<Long, List<IntimacyScore>> entry : allUserScores.entrySet()) {
            String userKey = USER_RANKING_KEY_PREFIX + entry.getKey();
            redisTemplate.opsForList().rightPushAll(userKey, entry.getValue().toArray());
            redisTemplate.expire(userKey, 7, TimeUnit.DAYS);
        }

        // 保存家庭群组排名，确保去重
        for (Map.Entry<Long, List<Long>> entry : familyGroupMembers.entrySet()) {
            Long familyGroupId = entry.getKey();
            List<Long> memberIds = entry.getValue();

            // 收集每个成员的最高亲密度记录
            Map<Long, IntimacyScore> bestScores = new HashMap<>();
            for (Long memberId : memberIds) {
                List<IntimacyScore> memberScores = allUserScores.getOrDefault(memberId, new ArrayList<>());
                if (!memberScores.isEmpty()) {
                    bestScores.put(memberId, memberScores.get(0));
                }
            }

            // 转换为列表并排序
            List<IntimacyScore> familyScores = new ArrayList<>(bestScores.values());
            familyScores.sort(Comparator.comparing(IntimacyScore::getScore).reversed());

            // 保存到Redis
            String familyKey = FAMILY_RANKING_KEY_PREFIX + familyGroupId;
            redisTemplate.opsForList().rightPushAll(familyKey, familyScores.toArray());
            redisTemplate.expire(familyKey, 7, TimeUnit.DAYS);
        }

        // 保存历史趋势数据
        String today = java.time.LocalDate.now().toString();
        for (Map.Entry<Long, List<IntimacyScore>> entry : allUserScores.entrySet()) {
            Long userId = entry.getKey();
            List<IntimacyScore> scores = entry.getValue();
            if (scores != null && !scores.isEmpty()) {
                // 取最高分
                double maxScore = scores.get(0).getScore();
                double percentage = Math.min(100.0, (maxScore / BASE_SCORE_FOR_100) * 100.0);
                percentage = Math.round(percentage * 10.0) / 10.0;
                
                // 存入Redis Hash: key=intimacy:history:{userId}, field=date, value=percentage
                redisTemplate.opsForHash().put(INTIMACY_HISTORY_KEY_PREFIX + userId, today, String.valueOf(percentage));
            }
        }

        // 设置全局排名的过期时间
        redisTemplate.expire(GLOBAL_RANKING_KEY, 7, TimeUnit.DAYS);
    }

    /**
     * 获取全网亲密度排名
     * 修改：确保排名连续且唯一
     */
    public List<IntimacyRankingVO> getGlobalRanking() {
        // 从Redis获取全局排名
        List<Object> cachedRanking = redisTemplate.opsForList().range(GLOBAL_RANKING_KEY, 0, -1);

        if (cachedRanking == null || cachedRanking.isEmpty()) {
            // 如果没有缓存，重新计算
            calculateAndSaveAllIntimacyScores();
            cachedRanking = redisTemplate.opsForList().range(GLOBAL_RANKING_KEY, 0, -1);
        }

        // 转换为VO对象
        List<IntimacyRankingVO> result = new ArrayList<>();
        Set<Long> processedUserIds = new HashSet<>();

        if (cachedRanking != null) {
            // 首先收集所有未处理过的用户分数
            List<IntimacyScore> scores = new ArrayList<>();
            for (Object obj : cachedRanking) {
                IntimacyScore score = (IntimacyScore) obj;
                if (!processedUserIds.contains(score.getUserId())) {
                    scores.add(score);
                    processedUserIds.add(score.getUserId());
                }
            }

            // 重新排序
            scores.sort(Comparator.comparing(IntimacyScore::getScore).reversed());

            // 重新计算百分比
            recalculatePercentages(scores);

            // 生成结果
            for (int i = 0; i < scores.size() && i < MAX_RANKING_SIZE; i++) {
                IntimacyScore score = scores.get(i);
                IntimacyRankingVO vo = new IntimacyRankingVO();
                vo.setRank(i + 1); // 确保排名连续
                vo.setNickname(score.getNickname());
                vo.setAvatar(score.getAvatar());
                vo.setPercentage(score.getPercentage());
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 获取用户所在家庭群组的亲密度排名
     * 修改：确保排名连续且唯一
     */
    public List<IntimacyRankingVO> getUserRanking(Long userId) {
        // 获取用户所在的家庭群组ID
        Long familyGroupId = familyRelationMapper.getUserFamilyGroupId(userId);

        if (familyGroupId == null) {
            log.info("用户 {} 没有家庭群组", userId);
            // 如果没有家庭群组，返回个人排名
            return getUserPersonalRanking(userId);
        }

        // 从Redis获取家庭群组排名
        String familyKey = FAMILY_RANKING_KEY_PREFIX + familyGroupId;
        List<Object> cachedRanking = redisTemplate.opsForList().range(familyKey, 0, -1);

        if (cachedRanking == null || cachedRanking.isEmpty()) {
            // 如果没有缓存，重新计算
            calculateAndSaveAllIntimacyScores();
            cachedRanking = redisTemplate.opsForList().range(familyKey, 0, -1);
        }

        // 转换为VO对象
        List<IntimacyRankingVO> result = new ArrayList<>();
        Set<Long> processedUserIds = new HashSet<>();

        if (cachedRanking != null) {
            // 首先收集所有未处理过的用户分数
            List<IntimacyScore> scores = new ArrayList<>();
            for (Object obj : cachedRanking) {
                IntimacyScore score = (IntimacyScore) obj;
                if (!processedUserIds.contains(score.getUserId())) {
                    scores.add(score);
                    processedUserIds.add(score.getUserId());
                }
            }

            // 重新排序
            scores.sort(Comparator.comparing(IntimacyScore::getScore).reversed());

            // 重新计算百分比
            recalculatePercentages(scores);

            // 生成结果
            for (int i = 0; i < scores.size(); i++) {
                IntimacyScore score = scores.get(i);
                IntimacyRankingVO vo = new IntimacyRankingVO();
                vo.setRank(i + 1); // 确保排名连续
                vo.setNickname(score.getNickname());
                vo.setAvatar(score.getAvatar());
                vo.setPercentage(score.getPercentage());
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 重新计算百分比，不再强制归一化，而是基于基准分
     */
    private void recalculatePercentages(List<IntimacyScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return;
        }

        for (IntimacyScore score : scores) {
            // 基于基准分计算百分比
            double percentage = (score.getScore() / BASE_SCORE_FOR_100) * 100.0;
            // 限制在 0-100 之间，保留一位小数
            percentage = Math.min(100.0, Math.max(0.0, percentage));
            score.setPercentage(Math.round(percentage * 10.0) / 10.0);
        }
    }

    /**
     * 获取用户个人亲密度排名（当没有家庭群组时使用）
     * 修改：确保排名连续
     */
    private List<IntimacyRankingVO> getUserPersonalRanking(Long userId) {
        String userKey = USER_RANKING_KEY_PREFIX + userId;

        // 从Redis获取用户个人排名
        List<Object> cachedRanking = redisTemplate.opsForList().range(userKey, 0, -1);

        if (cachedRanking == null || cachedRanking.isEmpty()) {
            // 如果没有缓存，重新计算
            calculateAndSaveAllIntimacyScores();
            cachedRanking = redisTemplate.opsForList().range(userKey, 0, -1);
        }

        // 转换为VO对象，确保亲属不重复
        List<IntimacyRankingVO> result = new ArrayList<>();
        Set<Long> processedRelatives = new HashSet<>();

        if (cachedRanking != null) {
            // 首先收集所有未处理过的亲属分数
            List<IntimacyScore> scores = new ArrayList<>();
            for (Object obj : cachedRanking) {
                IntimacyScore score = (IntimacyScore) obj;
                if (!processedRelatives.contains(score.getRelativeId())) {
                    scores.add(score);
                    processedRelatives.add(score.getRelativeId());
                }
            }

            // 重新排序
            scores.sort(Comparator.comparing(IntimacyScore::getScore).reversed());

            // 重新计算百分比
            recalculatePercentages(scores);

            // 生成结果
            for (int i = 0; i < scores.size(); i++) {
                IntimacyScore score = scores.get(i);
                IntimacyRankingVO vo = new IntimacyRankingVO();
                vo.setRank(i + 1); // 确保排名连续
                vo.setNickname(score.getNickname());
                vo.setAvatar(score.getAvatar());
                vo.setPercentage(score.getPercentage());
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 获取用户在全网的排名和亲密度
     */
    public Map<String, Object> getUserGlobalInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 排除测试用户
        if (userId == -999L || (userId != null && userId.equals(UserConstant.TEST_USER_ID))) {
            result.put("globalRank", 0);
            result.put("intimacyPercentage", 0.0);
            result.put("ranking", new ArrayList<>());
            return result;
        }

        // 尝试从Redis Hash中直接获取用户的排名信息
        Object rankInfoObj = redisTemplate.opsForHash().get(GLOBAL_USER_RANKS_KEY, String.valueOf(userId));

        if (rankInfoObj == null) {
            // 如果缓存不存在，可能是还没计算或者过期，重新计算
            calculateAndSaveAllIntimacyScores();
            rankInfoObj = redisTemplate.opsForHash().get(GLOBAL_USER_RANKS_KEY, String.valueOf(userId));
        }

        Integer globalRank = 0;
        Double intimacyPercentage = 0.0;

        if (rankInfoObj != null) {
            try {
                String rankInfo = rankInfoObj.toString();
                String[] parts = rankInfo.split(":");
                if (parts.length == 2) {
                    globalRank = Integer.parseInt(parts[0]);
                    intimacyPercentage = Double.parseDouble(parts[1]);
                }
            } catch (Exception e) {
                log.error("解析用户排名信息失败: {}", rankInfoObj, e);
            }
        }

        result.put("globalRank", globalRank);
        result.put("intimacyPercentage", intimacyPercentage);

        // 添加用户家庭排名
        result.put("ranking", getUserRanking(userId));

        return result;
    }
    
    /**
     * 清除缓存并重新计算亲密度
     */
    public void clearCacheAndRecalculate() {
        log.info("开始清除亲密度相关缓存");
        
        // 清除全网排行榜缓存
        redisTemplate.delete(GLOBAL_RANKING_KEY);
        
        // 清除所有家庭排行榜缓存
        Set<String> familyKeys = redisTemplate.keys(FAMILY_RANKING_KEY_PREFIX + "*");
        if (familyKeys != null && !familyKeys.isEmpty()) {
            redisTemplate.delete(familyKeys);
            log.info("清除了 {} 个家庭排行榜缓存", familyKeys.size());
        }
        
        // 清除所有用户排行榜缓存
        Set<String> userKeys = redisTemplate.keys(USER_RANKING_KEY_PREFIX + "*");
        if (userKeys != null && !userKeys.isEmpty()) {
            redisTemplate.delete(userKeys);
            log.info("清除了 {} 个用户排行榜缓存", userKeys.size());
        }
        
        log.info("缓存清除完成，开始重新计算亲密度");
        
        // 重新计算
        calculateAndSaveAllIntimacyScores();
        
        log.info("亲密度缓存清除和重新计算完成");
    }

    /**
     * 获取亲密度趋势数据
     * @param userId 用户ID
     * @param days 天数 (7 或 30)
     */
    public Map<String, Object> getIntimacyTrend(Long userId, int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        List<Double> average = new ArrayList<>(); 
        
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // 获取用户的历史数据
        Map<Object, Object> history = redisTemplate.opsForHash().entries(INTIMACY_HISTORY_KEY_PREFIX + userId);
        
        for (int i = days - 1; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            String dateStr = date.toString();
            
            // 简化标签
            if (days <= 7) {
                // 周几
                String dayOfWeek = "";
                switch (date.getDayOfWeek().getValue()) {
                    case 1: dayOfWeek = "周一"; break;
                    case 2: dayOfWeek = "周二"; break;
                    case 3: dayOfWeek = "周三"; break;
                    case 4: dayOfWeek = "周四"; break;
                    case 5: dayOfWeek = "周五"; break;
                    case 6: dayOfWeek = "周六"; break;
                    case 7: dayOfWeek = "周日"; break;
                }
                labels.add(dayOfWeek);
            } else {
                // MM-dd
                labels.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
            }
            
            // 获取数据
            double score = 0.0;
            if (history.containsKey(dateStr)) {
                try {
                    score = Double.parseDouble(history.get(dateStr).toString());
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            data.add(score);
            
            // 模拟平均值 (60-80之间波动，作为全网基准线)
            average.add(60.0 + (date.getDayOfYear() % 20)); 
        }
        
        result.put("labels", labels);
        result.put("data", data);
        result.put("average", average);
        
        return result;
    }
}