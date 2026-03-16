package com.qz.sns.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qz.sns.model.dto.SurveyDataPushDTO;
import com.qz.sns.model.entity.SurveyStarAnswer;
import com.qz.sns.model.entity.SurveyStarQuestionnaire;
import com.qz.sns.model.entity.SurveyUserStatistic;
import com.qz.sns.sv.result.Result;
import com.qz.sns.sv.result.ResultUtils;
import com.qz.sns.sv.service.ISurveyStarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 问卷星控制器
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/surveystar")
@RequiredArgsConstructor
public class SurveyStarController {

    private final ISurveyStarService surveyStarService;

    /**
     * 生成问卷链接
     *
     * @param surveyId 问卷ID（问卷星的问卷ID）
     * @param userId 用户ID（可选，如果不提供则从session获取）
     * @param source 来源标记（可选，如 'child-app'、'admin'）
     * @return 问卷链接
     */
    @GetMapping("/generate-link")
    public Result<Map<String, Object>> generateLink(
            @RequestParam("surveyId") Long surveyId,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "source", required = false, defaultValue = "app") String source) {
        
        log.info("🔗 生成问卷链接请求，问卷ID: {}, 用户ID: {}, 来源: {}", surveyId, userId, source);
        
        try {
            // 如果没有提供userId，可以从session中获取
            if (userId == null || userId.isEmpty()) {
                // TODO: 从session中获取当前用户ID
                // userId = getCurrentUserId();
                log.warn("⚠️ 未提供用户ID，使用默认值");
                userId = "0";
            }

            Map<String, Object> result = surveyStarService.generateSurveyLink(surveyId, userId, source);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("❌ 生成问卷链接失败", e);
            return ResultUtils.error(500, "生成问卷链接失败: " + e.getMessage());
        }
    }

    /**
     * 获取问卷列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param title 问卷标题（可选）
     * @param status 状态（可选，1-进行中 2-已结束 3-未发布）
     * @param type 问卷类型（可选，reading-阅读调查 feedback-用户反馈 experience-功能体验 other-其他）
     * @return 问卷列表
     */
    @GetMapping("/list")
    public Result<IPage<SurveyStarQuestionnaire>> getQuestionnaireList(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "type", required = false) String type) {
        
        log.info("📋 获取问卷列表请求，页码: {}, 每页大小: {}, 标题: {}, 状态: {}, 类型: {}", current, size, title, status, type);
        
        try {
            IPage<SurveyStarQuestionnaire> result = surveyStarService.getQuestionnaireList(current, size, title, status, type);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("❌ 获取问卷列表失败", e);
            return ResultUtils.error(500, "获取问卷列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取问卷详情
     *
     * @param surveyId 问卷ID
     * @return 问卷详情
     */
    @GetMapping("/{surveyId}")
    public Result<SurveyStarQuestionnaire> getQuestionnaireDetail(@PathVariable("surveyId") Long surveyId) {
        log.info("📄 获取问卷详情请求，问卷ID: {}", surveyId);
        
        try {
            SurveyStarQuestionnaire questionnaire = surveyStarService.getQuestionnaireDetail(surveyId);
            return ResultUtils.success(questionnaire);
        } catch (Exception e) {
            log.error("❌ 获取问卷详情失败", e);
            return ResultUtils.error(500, "获取问卷详情失败: " + e.getMessage());
        }
    }

    /**
     * 接收问卷星数据推送（Webhook）
     * 简化模式：仅更新问卷回答数，不存储详细答案数据
     * 详细数据请在问卷星网站查看
     *
     * @param dataPushDTO 推送数据
     * @return 处理结果
     */
    @PostMapping("/webhook/data-push")
    public Map<String, Object> receiveDataPush(@RequestBody SurveyDataPushDTO dataPushDTO) {
        log.info("📨 接收问卷星数据推送（简化模式），问卷ID: {}, 答卷ID: {}", dataPushDTO.getQuestid(), dataPushDTO.getId());
        
        try {
            Map<String, Object> result = surveyStarService.receiveDataPush(dataPushDTO);
            log.info("✅ 数据推送处理完成（仅更新回答数），结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("❌ 处理数据推送失败", e);
            Map<String, Object> errorResult = new java.util.HashMap<>();
            errorResult.put("success", false);
            errorResult.put("msg", "处理失败: " + e.getMessage());
            errorResult.put("code", -1);
            return errorResult;
        }
    }

    /**
     * 获取问卷答卷列表（已废弃：详细数据请在问卷星网站查看）
     *
     * @param questId 问卷ID
     * @param current 当前页
     * @param size 每页大小
     * @return 答卷列表
     * @deprecated 详细数据请在问卷星网站查看，此接口仅保留用于兼容
     */
    @Deprecated
    @GetMapping("/{questId}/answers")
    public Result<IPage<SurveyStarAnswer>> getAnswerList(
            @PathVariable("questId") Long questId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        
        log.warn("⚠️ 已废弃的接口调用：获取问卷答卷列表，问卷ID: {}，建议在问卷星网站查看详细数据", questId);
        
        try {
            IPage<SurveyStarAnswer> result = surveyStarService.getAnswerList(questId, current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("❌ 获取答卷列表失败", e);
            return ResultUtils.error(500, "获取答卷列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户问卷参与统计（已废弃：简化模式下不需要用户统计）
     *
     * @param userId 用户ID
     * @return 统计信息
     * @deprecated 简化模式下不需要用户统计，此接口仅保留用于兼容
     */
    @Deprecated
    @GetMapping("/user/{userId}/statistics")
    public Result<SurveyUserStatistic> getUserStatistics(@PathVariable("userId") Long userId) {
        log.warn("⚠️ 已废弃的接口调用：获取用户问卷统计，用户ID: {}", userId);
        
        try {
            SurveyUserStatistic statistic = surveyStarService.getUserStatistics(userId);
            return ResultUtils.success(statistic);
        } catch (Exception e) {
            log.error("❌ 获取用户统计失败", e);
            return ResultUtils.error(500, "获取用户统计失败: " + e.getMessage());
        }
    }

    /**
     * 创建问卷记录
     *
     * @param questionnaire 问卷信息
     * @return 是否成功
     */
    @PostMapping("/create")
    public Result<String> createQuestionnaire(@RequestBody SurveyStarQuestionnaire questionnaire) {
        log.info("➕ 创建问卷记录请求，问卷ID: {}, 标题: {}", questionnaire.getQuestId(), questionnaire.getTitle());
        
        try {
            boolean success = surveyStarService.createQuestionnaire(questionnaire);
            if (success) {
                return ResultUtils.success("问卷创建成功");
            } else {
                return ResultUtils.error(400, "问卷创建失败，可能已存在");
            }
        } catch (Exception e) {
            log.error("❌ 创建问卷失败", e);
            return ResultUtils.error(500, "创建问卷失败: " + e.getMessage());
        }
    }

    /**
     * 更新问卷信息
     *
     * @param id 问卷记录ID
     * @param questionnaire 问卷信息
     * @return 是否成功
     */
    @PutMapping("/{id}")
    public Result<String> updateQuestionnaire(
            @PathVariable("id") Long id,
            @RequestBody SurveyStarQuestionnaire questionnaire) {
        log.info("✏️ 更新问卷记录请求，ID: {}, 标题: {}", id, questionnaire.getTitle());
        
        try {
            questionnaire.setId(id);
            boolean success = surveyStarService.updateQuestionnaire(questionnaire);
            if (success) {
                return ResultUtils.success("问卷更新成功");
            } else {
                return ResultUtils.error(400, "问卷更新失败");
            }
        } catch (Exception e) {
            log.error("❌ 更新问卷失败", e);
            return ResultUtils.error(500, "更新问卷失败: " + e.getMessage());
        }
    }

    /**
     * 删除问卷记录
     *
     * @param id 问卷记录ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteQuestionnaire(@PathVariable("id") Long id) {
        log.info("🗑️ 删除问卷记录请求，ID: {}", id);
        
        try {
            boolean success = surveyStarService.deleteQuestionnaire(id);
            if (success) {
                return ResultUtils.success("问卷删除成功");
            } else {
                return ResultUtils.error(400, "问卷删除失败");
            }
        } catch (Exception e) {
            log.error("❌ 删除问卷失败", e);
            return ResultUtils.error(500, "删除问卷失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除问卷记录
     *
     * @param ids 问卷记录ID列表
     * @return 删除结果
     */
    @PostMapping("/batch-delete")
    public Result<Map<String, Object>> batchDeleteQuestionnaires(@RequestBody Map<String, java.util.List<Long>> request) {
        java.util.List<Long> ids = request.get("ids");
        log.info("🗑️ 批量删除问卷记录请求，IDs: {}", ids);
        
        try {
            if (ids == null || ids.isEmpty()) {
                return ResultUtils.error(400, "请选择要删除的问卷");
            }
            
            int deletedCount = surveyStarService.batchDeleteQuestionnaires(ids);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("deletedCount", deletedCount);
            result.put("totalCount", ids.size());
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("❌ 批量删除问卷失败", e);
            return ResultUtils.error(500, "批量删除问卷失败: " + e.getMessage());
        }
    }

    /**
     * 更新问卷状态
     *
     * @param id 问卷记录ID
     * @param request 包含status的请求体
     * @return 是否成功
     */
    @PutMapping("/{id}/status")
    public Result<String> updateQuestionnaireStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        log.info("🔄 更新问卷状态请求，ID: {}, 新状态: {}", id, status);
        
        try {
            if (status == null) {
                return ResultUtils.error(400, "状态参数不能为空");
            }
            
            boolean success = surveyStarService.updateQuestionnaireStatus(id, status);
            if (success) {
                return ResultUtils.success("状态更新成功");
            } else {
                return ResultUtils.error(400, "状态更新失败");
            }
        } catch (Exception e) {
            log.error("❌ 更新问卷状态失败", e);
            return ResultUtils.error(500, "更新问卷状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        log.info("📊 获取问卷统计数据请求");
        
        try {
            Map<String, Object> stats = surveyStarService.getStatistics();
            return ResultUtils.success(stats);
        } catch (Exception e) {
            log.error("❌ 获取统计数据失败", e);
            return ResultUtils.error(500, "获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否完成某个问卷
     *
     * @param questId 问卷ID
     * @param userId 用户ID
     * @return 是否完成
     */
    @GetMapping("/check-completed")
    public Result<Boolean> checkUserCompletedSurvey(
            @RequestParam("questId") Long questId,
            @RequestParam("userId") String userId) {
        log.info("🔍 检查用户完成状态请求，问卷ID: {}, 用户ID: {}", questId, userId);
        
        try {
            boolean completed = surveyStarService.checkUserCompletedSurvey(questId, userId);
            return ResultUtils.success(completed);
        } catch (Exception e) {
            log.error("❌ 检查用户完成状态失败", e);
            return ResultUtils.error(500, "检查用户完成状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量检查用户完成状态（用于前端列表显示）
     *
     * @param userId 用户ID
     * @param questIds 问卷ID列表
     * @return 已完成的问卷ID列表
     */
    @PostMapping("/check-completed-batch")
    public Result<java.util.List<Long>> getCompletedQuestIds(
            @RequestParam("userId") String userId,
            @RequestBody java.util.List<Long> questIds) {
        log.info("🔍 批量检查用户完成状态请求，用户ID: {}, 问卷数量: {}", userId, questIds != null ? questIds.size() : 0);
        
        try {
            java.util.List<Long> completedIds = surveyStarService.getCompletedQuestIds(userId, questIds);
            return ResultUtils.success(completedIds);
        } catch (Exception e) {
            log.error("❌ 批量检查用户完成状态失败", e);
            return ResultUtils.error(500, "批量检查用户完成状态失败: " + e.getMessage());
        }
    }

    /**
     * 手动标记用户已完成问卷（用于用户主动确认完成）
     *
     * @param questId 问卷ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @PostMapping("/mark-completed")
    public Result<Boolean> markCompleted(
            @RequestParam("questId") Long questId,
            @RequestParam("userId") String userId) {
        log.info("✅ 手动标记用户完成问卷请求，问卷ID: {}, 用户ID: {}", questId, userId);
        
        try {
            boolean success = surveyStarService.markCompleted(questId, userId);
            if (success) {
                return ResultUtils.success(true);
            } else {
                return ResultUtils.error(400, "标记完成失败");
            }
        } catch (Exception e) {
            log.error("❌ 标记用户完成问卷失败", e);
            return ResultUtils.error(500, "标记完成失败: " + e.getMessage());
        }
    }
}

