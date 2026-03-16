package com.qz.sns.sv.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qz.sns.model.entity.SurveyStarAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 问卷星答卷记录表 Mapper 接口
 * </p>
 *
 * @author System
 * @since 2025-01-01
 */
@Mapper
@Repository
public interface SurveyStarAnswerMapper extends BaseMapper<SurveyStarAnswer> {

    /**
     * 分页查询问卷答卷列表
     *
     * @param page 分页参数
     * @param questId 问卷ID
     * @return 分页结果
     */
    IPage<SurveyStarAnswer> selectAnswerPageByQuestId(
            Page<SurveyStarAnswer> page,
            @Param("questId") Long questId
    );

    /**
     * 根据答卷ID查询答卷
     *
     * @param answerId 答卷ID
     * @return 答卷信息
     */
    SurveyStarAnswer selectByAnswerId(@Param("answerId") String answerId);

    /**
     * 根据用户ID查询答卷列表
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<SurveyStarAnswer> selectAnswerPageByUserId(
            Page<SurveyStarAnswer> page,
            @Param("userId") String userId
    );

    /**
     * 检查用户是否完成某个问卷
     *
     * @param questId 问卷ID
     * @param userId 用户ID
     * @return 答卷记录（如果存在）
     */
    SurveyStarAnswer selectByQuestIdAndUserId(
            @Param("questId") Long questId,
            @Param("userId") String userId
    );

    /**
     * 批量检查用户完成状态（用于前端列表显示）
     *
     * @param questIds 问卷ID列表
     * @param userId 用户ID
     * @return 已完成的问卷ID列表
     */
    java.util.List<Long> selectCompletedQuestIdsByUserId(
            @Param("questIds") java.util.List<Long> questIds,
            @Param("userId") String userId
    );
}

