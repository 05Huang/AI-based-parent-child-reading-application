package com.qz.sns.sv.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qz.sns.model.dto.SurveyDataPushDTO;
import com.qz.sns.model.entity.SurveyStarAnswer;
import com.qz.sns.model.entity.SurveyStarQuestionnaire;
import com.qz.sns.model.entity.SurveyUserStatistic;

import java.util.Map;

/**
 * <p>
 * 问卷星服务类
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
public interface ISurveyStarService extends IService<SurveyStarQuestionnaire> {

    /**
     * 生成问卷链接
     *
     * @param surveyId 问卷ID
     * @param userId 用户ID
     * @param source 来源标记
     * @return 包含问卷链接的Map
     */
    Map<String, Object> generateSurveyLink(Long surveyId, String userId, String source);

    /**
     * 分页查询问卷列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param title 问卷标题（可选）
     * @param status 状态（可选）
     * @param type 问卷类型（可选）
     * @return 分页结果
     */
    IPage<SurveyStarQuestionnaire> getQuestionnaireList(Integer current, Integer size, String title, Integer status, String type);

    /**
     * 根据问卷ID获取问卷详情
     *
     * @param surveyId 问卷ID
     * @return 问卷详情
     */
    SurveyStarQuestionnaire getQuestionnaireDetail(Long surveyId);

    /**
     * 接收问卷星数据推送
     *
     * @param dataPushDTO 推送数据
     * @return 处理结果
     */
    Map<String, Object> receiveDataPush(SurveyDataPushDTO dataPushDTO);

    /**
     * 分页查询问卷答卷列表
     *
     * @param questId 问卷ID
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<SurveyStarAnswer> getAnswerList(Long questId, Integer current, Integer size);

    /**
     * 获取用户问卷参与统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    SurveyUserStatistic getUserStatistics(Long userId);

    /**
     * 创建问卷记录
     *
     * @param questionnaire 问卷信息
     * @return 是否成功
     */
    boolean createQuestionnaire(SurveyStarQuestionnaire questionnaire);

    /**
     * 更新问卷信息
     *
     * @param questionnaire 问卷信息
     * @return 是否成功
     */
    boolean updateQuestionnaire(SurveyStarQuestionnaire questionnaire);

    /**
     * 删除问卷记录
     *
     * @param id 问卷记录ID
     * @return 是否成功
     */
    boolean deleteQuestionnaire(Long id);

    /**
     * 批量删除问卷记录
     *
     * @param ids 问卷记录ID列表
     * @return 删除成功的数量
     */
    int batchDeleteQuestionnaires(java.util.List<Long> ids);

    /**
     * 更新问卷状态
     *
     * @param id 问卷记录ID
     * @param status 新状态（1-进行中 2-已结束 3-未发布）
     * @return 是否成功
     */
    boolean updateQuestionnaireStatus(Long id, Integer status);

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getStatistics();

    /**
     * 检查用户是否完成某个问卷
     *
     * @param questId 问卷ID
     * @param userId 用户ID
     * @return 是否完成
     */
    boolean checkUserCompletedSurvey(Long questId, String userId);

    /**
     * 批量检查用户完成状态（用于前端列表显示）
     *
     * @param questIds 问卷ID列表
     * @param userId 用户ID
     * @return 已完成的问卷ID列表
     */
    java.util.List<Long> getCompletedQuestIds(String userId, java.util.List<Long> questIds);

    /**
     * 手动标记用户已完成问卷（用于用户主动确认完成）
     *
     * @param questId 问卷ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markCompleted(Long questId, String userId);
}

