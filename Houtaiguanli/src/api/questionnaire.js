import request from '@/utils/request'

// ========== 问卷星相关API ==========

// 获取问卷星问卷列表
export function getSurveyStarList(params) {
  return request({
    url: '/api/surveystar/list',
    method: 'get',
    params
  })
}

// 获取问卷星问卷详情
export function getSurveyStarDetail(surveyId) {
  return request({
    url: `/api/surveystar/${surveyId}`,
    method: 'get'
  })
}

// 创建问卷星问卷记录
export function createSurveyStar(data) {
  return request({
    url: '/api/surveystar/create',
    method: 'post',
    data
  })
}

// 更新问卷星问卷信息
export function updateSurveyStar(id, data) {
  return request({
    url: `/api/surveystar/${id}`,
    method: 'put',
    data
  })
}

// 删除问卷星问卷
export function deleteSurveyStar(id) {
  return request({
    url: `/api/surveystar/${id}`,
    method: 'delete'
  })
}

// 批量删除问卷星问卷
export function batchDeleteSurveyStar(ids) {
  return request({
    url: '/api/surveystar/batch-delete',
    method: 'post',
    data: { ids }
  })
}

// 更新问卷状态
export function updateSurveyStarStatus(id, status) {
  return request({
    url: `/api/surveystar/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 获取统计数据
export function getSurveyStarStatistics() {
  return request({
    url: '/api/surveystar/statistics',
    method: 'get'
  })
}

// 获取问卷星答卷列表
export function getSurveyStarAnswers(questId, params) {
  return request({
    url: `/api/surveystar/${questId}/answers`,
    method: 'get',
    params
  })
}

// 生成问卷星链接
export function generateSurveyStarLink(surveyId, userId, source) {
  return request({
    url: '/api/surveystar/generate-link',
    method: 'get',
    params: {
      surveyId,
      userId,
      source
    }
  })
}

// ========== 原有问卷API（保留兼容） ==========

// 获取问卷列表
export function getQuestionnaireList(params) {
  return request({
    url: '/api/questionnaire/list',
    method: 'get',
    params
  })
}

// 获取问卷详情
export function getQuestionnaireDetail(id) {
  return request({
    url: `/api/questionnaire/${id}`,
    method: 'get'
  })
}

// 创建问卷
export function createQuestionnaire(data) {
  return request({
    url: '/api/questionnaire/create',
    method: 'post',
    data
  })
}

// 更新问卷
export function updateQuestionnaire(id, data) {
  return request({
    url: `/api/questionnaire/${id}`,
    method: 'put',
    data
  })
}

// 删除问卷
export function deleteQuestionnaire(id) {
  return request({
    url: `/api/questionnaire/${id}`,
    method: 'delete'
  })
}

// 批量删除问卷
export function batchDeleteQuestionnaire(ids) {
  return request({
    url: '/api/questionnaire/batch-delete',
    method: 'post',
    data: { ids }
  })
}

// 发布问卷
export function publishQuestionnaire(id) {
  return request({
    url: `/api/questionnaire/${id}/publish`,
    method: 'post'
  })
}

// 关闭问卷
export function closeQuestionnaire(id) {
  return request({
    url: `/api/questionnaire/${id}/close`,
    method: 'post'
  })
}

// 获取问卷回收数据
export function getQuestionnaireResponses(id, params) {
  return request({
    url: `/api/questionnaire/${id}/responses`,
    method: 'get',
    params
  })
}

// 获取问卷统计数据
export function getQuestionnaireStats(id) {
  return request({
    url: `/api/questionnaire/${id}/stats`,
    method: 'get'
  })
}

// 导出问卷数据
export function exportQuestionnaireData(id) {
  return request({
    url: `/api/questionnaire/${id}/export`,
    method: 'get',
    responseType: 'blob'
  })
}