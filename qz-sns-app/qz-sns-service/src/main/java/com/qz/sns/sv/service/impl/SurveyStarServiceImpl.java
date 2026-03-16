package com.qz.sns.sv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qz.sns.model.dto.SurveyDataPushDTO;
import com.qz.sns.model.entity.SurveyStarAnswer;
import com.qz.sns.model.entity.SurveyStarQuestionnaire;
import com.qz.sns.model.entity.SurveyUserStatistic;
import com.qz.sns.sv.mapper.SurveyStarAnswerMapper;
import com.qz.sns.sv.mapper.SurveyStarQuestionnaireMapper;
import com.qz.sns.sv.mapper.SurveyUserStatisticMapper;
import com.qz.sns.sv.service.ISurveyStarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 问卷星服务实现类
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyStarServiceImpl extends ServiceImpl<SurveyStarQuestionnaireMapper, SurveyStarQuestionnaire> implements ISurveyStarService {

    private final SurveyStarQuestionnaireMapper questionnaireMapper;
    private final SurveyStarAnswerMapper answerMapper;
    private final SurveyUserStatisticMapper statisticMapper;

    @Override
    public Map<String, Object> generateSurveyLink(Long surveyId, String userId, String source) {
        log.info("🔗 生成问卷链接，问卷ID: {}, 用户ID: {}, 来源: {}", surveyId, userId, source);

        // 查询问卷信息
        SurveyStarQuestionnaire questionnaire = questionnaireMapper.selectByQuestId(surveyId);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在，问卷ID: " + surveyId);
        }

        // 构建问卷链接
        String baseUrl = questionnaire.getSurveyUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            // 如果没有配置URL，使用默认格式
            baseUrl = "https://www.wjx.cn/jq/" + surveyId + ".aspx";
        }

        // 添加自定义参数
        StringBuilder linkBuilder = new StringBuilder(baseUrl);
        if (!baseUrl.contains("?")) {
            linkBuilder.append("?");
        } else {
            linkBuilder.append("&");
        }
        linkBuilder.append("userid=").append(userId);
        if (StringUtils.hasText(source)) {
            linkBuilder.append("&source=").append(source);
        }

        String link = linkBuilder.toString();
        String embedUrl = link + "&embedded=1";

        log.info("✅ 问卷链接生成成功: {}", link);

        Map<String, Object> result = new HashMap<>();
        result.put("surveyId", surveyId);
        result.put("link", link);
        result.put("embedUrl", embedUrl);
        return result;
    }

    @Override
    public IPage<SurveyStarQuestionnaire> getQuestionnaireList(Integer current, Integer size, String title, Integer status, String type) {
        log.info("📋 获取问卷列表，页码: {}, 每页大小: {}, 标题: {}, 状态: {}, 类型: {}", current, size, title, status, type);

        Page<SurveyStarQuestionnaire> page = new Page<>(current, size);
        IPage<SurveyStarQuestionnaire> result = questionnaireMapper.selectQuestionnairePage(page, title, status, type);

        log.info("✅ 获取到 {} 条问卷记录", result.getRecords().size());
        return result;
    }

    @Override
    public SurveyStarQuestionnaire getQuestionnaireDetail(Long surveyId) {
        log.info("📄 获取问卷详情，问卷ID: {}", surveyId);

        // 先尝试通过ID查询（可能是数据库记录ID）
        SurveyStarQuestionnaire questionnaire = questionnaireMapper.selectById(surveyId);
        
        // 如果通过ID查不到，尝试通过questId查询（可能是问卷星问卷ID）
        if (questionnaire == null) {
            questionnaire = questionnaireMapper.selectByQuestId(surveyId);
        }
        
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在，问卷ID: " + surveyId);
        }

        log.info("✅ 获取问卷详情成功: {}", questionnaire.getTitle());
        return questionnaire;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> receiveDataPush(SurveyDataPushDTO dataPushDTO) {
        log.info("📨 接收问卷星数据推送（简化模式：仅更新回答数），问卷ID: {}, 答卷ID: {}", 
                dataPushDTO.getQuestid(), dataPushDTO.getId());

        try {
            // 简化模式：只更新问卷的回答数，不存储详细答案数据
            // 详细数据在问卷星网站查看，我们系统只记录回答数
            
            // 检查答卷是否已存在（防止重复计数）
            SurveyStarAnswer existingAnswer = answerMapper.selectByAnswerId(String.valueOf(dataPushDTO.getId()));
            if (existingAnswer != null) {
                log.warn("⚠️ 答卷已存在，跳过处理，答卷ID: {}", dataPushDTO.getId());
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("msg", "数据已存在，跳过处理");
                result.put("code", 0);
                return result;
            }

            // 可选：保存最小化的答卷记录（仅用于去重和计数）
            // 如果不需要存储任何数据，可以注释掉以下代码
            SurveyStarAnswer answer = new SurveyStarAnswer();
            answer.setAnswerId(String.valueOf(dataPushDTO.getId()));
            answer.setQuestId(dataPushDTO.getQuestid());
            answer.setUserId(dataPushDTO.getUserid());
            // 不存储详细答案数据，只记录基本信息
            answer.setAnswerData(null); // 不存储详细答案
            answer.setSubmitIp(dataPushDTO.getIp());
            
            // 转换时间戳为LocalDateTime
            if (dataPushDTO.getSubmittime() != null) {
                LocalDateTime submitTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(dataPushDTO.getSubmittime()),
                        ZoneId.systemDefault()
                );
                answer.setSubmitTime(submitTime);
            }
            
            answer.setReceivedTime(LocalDateTime.now());
            answer.setStatus(1); // 1-已保存
            answer.setCreatedAt(LocalDateTime.now());
            answer.setUpdatedAt(LocalDateTime.now());

            answerMapper.insert(answer);
            log.info("✅ 答卷记录保存成功（简化模式），答卷ID: {}", answer.getAnswerId());

            // 更新问卷回答数
            questionnaireMapper.incrementAnswerCount(dataPushDTO.getQuestid());
            log.info("✅ 问卷回答数已更新，问卷ID: {}", dataPushDTO.getQuestid());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("msg", "数据接收成功（仅更新回答数）");
            result.put("code", 0);
            return result;

        } catch (Exception e) {
            log.error("❌ 接收问卷星数据推送失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "数据接收失败: " + e.getMessage());
            result.put("code", -1);
            return result;
        }
    }

    @Override
    public IPage<SurveyStarAnswer> getAnswerList(Long questId, Integer current, Integer size) {
        log.info("📊 获取问卷答卷列表，问卷ID: {}, 页码: {}, 每页大小: {}", questId, current, size);

        Page<SurveyStarAnswer> page = new Page<>(current, size);
        IPage<SurveyStarAnswer> result = answerMapper.selectAnswerPageByQuestId(page, questId);

        log.info("✅ 获取到 {} 条答卷记录", result.getRecords().size());
        return result;
    }

    @Override
    public SurveyUserStatistic getUserStatistics(Long userId) {
        log.info("📈 获取用户问卷统计，用户ID: {}", userId);

        SurveyUserStatistic statistic = statisticMapper.selectByUserId(userId);
        if (statistic == null) {
            // 如果不存在，创建新的统计记录
            statistic = new SurveyUserStatistic();
            statistic.setUserId(userId);
            statistic.setTotalSurveys(0);
            statistic.setCompletedSurveys(0);
            statistic.setTotalPoints(0);
            statistic.setCreatedAt(LocalDateTime.now());
            statistic.setUpdatedAt(LocalDateTime.now());
            statisticMapper.insert(statistic);
            log.info("✅ 创建新的用户统计记录，用户ID: {}", userId);
        }

        return statistic;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createQuestionnaire(SurveyStarQuestionnaire questionnaire) {
        log.info("➕ 创建问卷记录，问卷ID: {}, 标题: {}", questionnaire.getQuestId(), questionnaire.getTitle());

        // 检查问卷是否已存在
        SurveyStarQuestionnaire existing = questionnaireMapper.selectByQuestId(questionnaire.getQuestId());
        if (existing != null) {
            log.warn("⚠️ 问卷已存在，问卷ID: {}", questionnaire.getQuestId());
            return false;
        }

        questionnaire.setCreatedAt(LocalDateTime.now());
        questionnaire.setUpdatedAt(LocalDateTime.now());
        if (questionnaire.getStatus() == null) {
            questionnaire.setStatus(1); // 默认状态：1-进行中
        }
        if (questionnaire.getAnswerCount() == null) {
            questionnaire.setAnswerCount(0);
        }

        int result = questionnaireMapper.insert(questionnaire);
        log.info("✅ 问卷记录创建{}，问卷ID: {}", result > 0 ? "成功" : "失败", questionnaire.getQuestId());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuestionnaire(SurveyStarQuestionnaire questionnaire) {
        log.info("✏️ 更新问卷记录，问卷ID: {}, 标题: {}", questionnaire.getQuestId(), questionnaire.getTitle());

        questionnaire.setUpdatedAt(LocalDateTime.now());
        int result = questionnaireMapper.updateById(questionnaire);
        log.info("✅ 问卷记录更新{}，问卷ID: {}", result > 0 ? "成功" : "失败", questionnaire.getQuestId());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteQuestionnaire(Long id) {
        log.info("🗑️ 删除问卷记录，ID: {}", id);

        // 检查是否有答卷数据
        LambdaQueryWrapper<SurveyStarAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(SurveyStarAnswer::getQuestId, id);
        Long answerCount = answerMapper.selectCount(answerWrapper);

        if (answerCount > 0) {
            log.warn("⚠️ 问卷有 {} 条答卷数据，但仍允许删除", answerCount);
        }

        int result = questionnaireMapper.deleteById(id);
        log.info("✅ 问卷记录删除{}，ID: {}", result > 0 ? "成功" : "失败", id);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteQuestionnaires(java.util.List<Long> ids) {
        log.info("🗑️ 批量删除问卷记录，IDs: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int deletedCount = 0;
        for (Long id : ids) {
            if (deleteQuestionnaire(id)) {
                deletedCount++;
            }
        }

        log.info("✅ 批量删除完成，成功删除 {} 条记录", deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuestionnaireStatus(Long id, Integer status) {
        log.info("🔄 更新问卷状态，ID: {}, 新状态: {}", id, status);

        SurveyStarQuestionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            throw new RuntimeException("问卷不存在，ID: " + id);
        }

        questionnaire.setStatus(status);
        questionnaire.setUpdatedAt(LocalDateTime.now());
        int result = questionnaireMapper.updateById(questionnaire);
        log.info("✅ 问卷状态更新{}，ID: {}", result > 0 ? "成功" : "失败", id);
        return result > 0;
    }

    @Override
    public Map<String, Object> getStatistics() {
        log.info("📊 获取问卷统计数据");

        Map<String, Object> stats = new HashMap<>();
        
        // 初始化默认值
        int totalQuestionnaires = 0;
        int publishedQuestionnaires = 0;
        int totalResponses = 0;

        try {
            // 查询所有问卷
            if (questionnaireMapper != null) {
                java.util.List<SurveyStarQuestionnaire> allQuestionnaires = questionnaireMapper.selectList(null);
                
                if (allQuestionnaires != null) {
                    totalQuestionnaires = allQuestionnaires.size();
                    publishedQuestionnaires = (int) allQuestionnaires.stream()
                            .filter(q -> q != null && q.getStatus() != null && q.getStatus() == 1)
                            .count();
                    totalResponses = allQuestionnaires.stream()
                            .filter(q -> q != null)
                            .mapToInt(q -> q.getAnswerCount() != null ? q.getAnswerCount() : 0)
                            .sum();
                }
            } else {
                log.warn("⚠️ questionnaireMapper 为 null");
            }
        } catch (Exception e) {
            log.error("❌ 查询问卷列表失败", e);
        }

        stats.put("totalQuestionnaires", totalQuestionnaires);
        stats.put("publishedQuestionnaires", publishedQuestionnaires);
        stats.put("totalResponses", totalResponses);

        log.info("✅ 统计数据：总数={}, 已发布={}, 总回收={}", 
                totalQuestionnaires, publishedQuestionnaires, totalResponses);
        return stats;
    }

    @Override
    public boolean checkUserCompletedSurvey(Long questId, String userId) {
        log.info("🔍 检查用户是否完成问卷，问卷ID: {}, 用户ID: {}", questId, userId);

        if (questId == null || userId == null || userId.trim().isEmpty()) {
            log.warn("⚠️ 参数不完整，问卷ID: {}, 用户ID: {}", questId, userId);
            return false;
        }

        try {
            SurveyStarAnswer answer = answerMapper.selectByQuestIdAndUserId(questId, userId);
            boolean completed = answer != null;
            log.info("✅ 检查完成，问卷ID: {}, 用户ID: {}, 完成状态: {}", questId, userId, completed);
            return completed;
        } catch (Exception e) {
            log.error("❌ 检查用户完成状态失败", e);
            return false;
        }
    }

    @Override
    public java.util.List<Long> getCompletedQuestIds(String userId, java.util.List<Long> questIds) {
        log.info("🔍 批量检查用户完成状态，用户ID: {}, 问卷数量: {}", userId, questIds != null ? questIds.size() : 0);

        if (userId == null || userId.trim().isEmpty() || questIds == null || questIds.isEmpty()) {
            log.warn("⚠️ 参数不完整，用户ID: {}, 问卷列表: {}", userId, questIds);
            return new java.util.ArrayList<>();
        }

        try {
            java.util.List<Long> completedIds = answerMapper.selectCompletedQuestIdsByUserId(questIds, userId);
            log.info("✅ 批量检查完成，用户ID: {}, 已完成问卷数: {}", userId, completedIds != null ? completedIds.size() : 0);
            return completedIds != null ? completedIds : new java.util.ArrayList<>();
        } catch (Exception e) {
            log.error("❌ 批量检查用户完成状态失败", e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markCompleted(Long questId, String userId) {
        log.info("✅ 手动标记用户完成问卷，问卷ID: {}, 用户ID: {}", questId, userId);

        if (questId == null || userId == null || userId.trim().isEmpty()) {
            log.warn("⚠️ 参数不完整，问卷ID: {}, 用户ID: {}", questId, userId);
            return false;
        }

        try {
            // 检查是否已经记录过
            SurveyStarAnswer existingAnswer = answerMapper.selectByQuestIdAndUserId(questId, userId);
            if (existingAnswer != null) {
                log.info("ℹ️ 用户已完成记录已存在，问卷ID: {}, 用户ID: {}", questId, userId);
                return true;
            }

            // 创建新的完成记录
            SurveyStarAnswer answer = new SurveyStarAnswer();
            // 生成唯一的 answer_id（使用时间戳+随机数）
            String answerId = "manual_" + questId + "_" + userId + "_" + System.currentTimeMillis();
            answer.setAnswerId(answerId);
            answer.setQuestId(questId);
            answer.setUserId(userId);
            answer.setAnswerData(null); // 不存储详细答案数据
            answer.setSubmitIp(null); // 手动标记时没有IP信息
            answer.setSubmitTime(LocalDateTime.now());
            answer.setReceivedTime(LocalDateTime.now());
            answer.setStatus(1); // 1-已保存
            answer.setCreatedAt(LocalDateTime.now());
            answer.setUpdatedAt(LocalDateTime.now());

            answerMapper.insert(answer);
            log.info("✅ 手动标记完成记录保存成功，问卷ID: {}, 用户ID: {}, 答卷ID: {}", questId, userId, answerId);

            // 更新问卷回答数
            questionnaireMapper.incrementAnswerCount(questId);
            log.info("✅ 问卷回答数已更新，问卷ID: {}", questId);

            return true;
        } catch (Exception e) {
            log.error("❌ 手动标记用户完成问卷失败", e);
            return false;
        }
    }
}

