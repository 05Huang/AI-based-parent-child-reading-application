package com.qz.sns.sv.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.entity.SurveyStarQuestionnaire;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 问卷星问卷信息表 Mapper 接口
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
@Mapper
@Repository
public interface SurveyStarQuestionnaireMapper extends BaseMapper<SurveyStarQuestionnaire> {

    /**
     * 分页查询问卷列表
     *
     * @param page 分页参数
     * @param title 问卷标题（模糊查询）
     * @param status 状态
     * @param type 问卷类型
     * @return 分页结果
     */
    IPage<SurveyStarQuestionnaire> selectQuestionnairePage(
            Page<SurveyStarQuestionnaire> page,
            @Param("title") String title,
            @Param("status") Integer status,
            @Param("type") String type
    );

    /**
     * 根据问卷星ID查询问卷
     *
     * @param questId 问卷星问卷ID
     * @return 问卷信息
     */
    SurveyStarQuestionnaire selectByQuestId(@Param("questId") Long questId);

    /**
     * 增加问卷回答数
     *
     * @param questId 问卷星问卷ID
     * @return 更新行数
     */
    int incrementAnswerCount(@Param("questId") Long questId);
}
