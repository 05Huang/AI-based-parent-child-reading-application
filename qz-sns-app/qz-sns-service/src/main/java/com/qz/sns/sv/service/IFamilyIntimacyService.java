package com.qz.sns.sv.service;

import com.qz.sns.model.vo.FamilyIntimacyAnalysisVO;

/**
 * 家庭关系亲密度分析服务接口
 */
public interface IFamilyIntimacyService {
    
    /**
     * 计算用户与所有家庭成员之间的亲密度分析
     * 
     * @param userId 用户ID
     * @return 亲密度分析结果
     */
    FamilyIntimacyAnalysisVO calculateIntimacyAnalysis(Long userId);
}

