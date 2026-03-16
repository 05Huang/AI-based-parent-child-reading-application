<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="问卷标题">
          <el-input v-model="searchForm.title" placeholder="请输入问卷标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="问卷状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已关闭" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="问卷类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable style="width: 200px">
            <el-option label="阅读调查" value="reading" />
            <el-option label="用户反馈" value="feedback" />
            <el-option label="功能体验" value="experience" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="operation-container">
        <el-button type="primary" :icon="Plus" @click="handleCreate">新建问卷</el-button>
        <el-button type="danger" :icon="Delete" :disabled="!selectedQuestionnaires.length" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalQuestionnaires }}</div>
          <div class="stat-label">问卷总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.publishedQuestionnaires }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalResponses }}</div>
          <div class="stat-label">总回收</div>
        </div>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="questionnaireList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        stripe
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="title" label="问卷标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="80" align="center" />
        <el-table-column prop="responseCount" label="回收数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column prop="publishTime" label="发布时间" width="160" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; justify-content: center; gap: 8px; flex-wrap: wrap;">
              <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-dropdown @command="(cmd) => handleViewData(cmd, row)" trigger="click">
                <el-button link type="info" size="small" style="display: inline-flex; align-items: center;">
                  查看数据<el-icon style="margin-left: 4px;"><arrow-down /></el-icon>
              </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="users">查看填写记录</el-dropdown-item>
                    <el-dropdown-item command="wenjuanxing">前往问卷星</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button 
                link 
                :type="row.status === 'published' ? 'warning' : 'success'" 
                size="small"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 'published' ? '关闭' : '发布' }}
              </el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 问卷编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editType === 'create' ? '新建问卷' : '编辑问卷'"
      width="900px"
      destroy-on-close
    >
      <el-form
        ref="questionnaireFormRef"
        :model="questionnaireForm"
        :rules="questionnaireRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="问卷标题" prop="title">
              <el-input v-model="questionnaireForm.title" placeholder="请输入问卷标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="问卷类型" prop="type">
              <el-select v-model="questionnaireForm.type" placeholder="请选择类型" style="width: 100%">
                <el-option label="阅读调查" value="reading" />
                <el-option label="用户反馈" value="feedback" />
                <el-option label="功能体验" value="experience" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="问卷描述" prop="description">
          <el-input
            v-model="questionnaireForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入问卷描述"
          />
        </el-form-item>
        
        <!-- 问卷星关联设置 -->
        <el-divider content-position="left">问卷星关联设置</el-divider>
        <el-alert
          title="提示：请在问卷星平台创建问卷后，填写以下信息进行关联"
          type="info"
          :closable="false"
          style="margin-bottom: 16px"
        />
              <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="问卷星报告ID" prop="questId">
                  <el-input
                v-model.number="questionnaireForm.questId" 
                placeholder="请输入问卷星报告ID（如：335617250）"
                type="number"
              >
                <template #append>
                  <el-button @click="openSurveyStarHelp" link>如何获取？</el-button>
                </template>
              </el-input>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                注意：报告ID用于查看数据统计，与问卷填写链接不同
              </div>
            </el-form-item>
                </el-col>
          <el-col :span="12">
            <el-form-item label="问卷状态" prop="status">
              <el-select v-model.number="questionnaireForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="进行中" :value="1" />
                <el-option label="已结束" :value="2" />
                <el-option label="未发布" :value="3" />
                  </el-select>
            </el-form-item>
                </el-col>
              </el-row>
        <el-form-item label="问卷星链接" prop="surveyUrl">
          <el-input 
            v-model="questionnaireForm.surveyUrl" 
            placeholder="请输入问卷星问卷链接（如：https://www.wjx.cn/vm/tUWRrL4.aspx）"
          >
            <template #append>
              <el-button @click="testSurveyLink" link>测试链接</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="备注">
                  <el-input
            v-model="questionnaireForm.remark" 
            type="textarea"
            :rows="2"
            placeholder="可选：填写备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="success" @click="handleSaveDraft">保存草稿</el-button>
          <el-button type="primary" @click="handleSaveAndPublish">保存并发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户填写记录对话框 -->
    <el-dialog
      v-model="userAnswersDialogVisible"
      title="用户填写记录"
      width="900px"
      destroy-on-close
    >
      <div v-loading="answersLoading" style="min-height: 200px;">
        <el-table :data="userAnswersList" stripe style="width: 100%">
          <el-table-column prop="userId" label="用户ID" width="120" />
          <el-table-column prop="submitTime" label="提交时间" width="180">
            <template #default="{ row }">
              {{ row.submitTime ? new Date(row.submitTime).toLocaleString() : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="submitIp" label="提交IP" width="150" />
          <el-table-column prop="answerId" label="答卷ID" width="150" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'" size="small">
                {{ row.status === 1 ? '已保存' : row.status === 2 ? '已处理' : '已分析' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="userAnswersList.length === 0 && !answersLoading" style="text-align: center; padding: 40px; color: #909399;">
          <el-icon style="font-size: 48px; margin-bottom: 16px;"><Document /></el-icon>
          <div>暂无填写记录</div>
                    </div>
        
        <div v-if="userAnswersList.length > 0" style="margin-top: 16px; text-align: right; color: #909399; font-size: 14px;">
          共 {{ userAnswersList.length }} 条记录
                      </div>
                    </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userAnswersDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Search,
  RefreshRight as Refresh,
  Plus,
  Edit,
  Delete,
  Document,
  Clock,
  User,
  ArrowDown
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getSurveyStarList, 
  createSurveyStar, 
  updateSurveyStar,
  deleteSurveyStar,
  batchDeleteSurveyStar,
  updateSurveyStarStatus,
  getSurveyStarStatistics,
  getSurveyStarAnswers
} from '@/api/questionnaire'

// 搜索表单
const searchForm = reactive({
  title: '',
  status: '',
  type: ''
})

// 统计数据（初始值为0，从API获取真实数据）
const stats = ref({
  totalQuestionnaires: 0,
  publishedQuestionnaires: 0,
  totalResponses: 0
})

// 表格数据（初始为空数组，从API获取真实数据）
const loading = ref(false)
const questionnaireList = ref([])
const selectedQuestionnaires = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 用户填写记录对话框
const userAnswersDialogVisible = ref(false)
const answersLoading = ref(false)
const userAnswersList = ref([])
const currentQuestId = ref(null)

// 编辑对话框
const editDialogVisible = ref(false)
const editType = ref('create')
const questionnaireFormRef = ref(null)
const questionnaireForm = reactive({
  title: '',
  type: '',
  description: '',
  // 问卷星相关字段
  questId: null,
  surveyUrl: '',
  status: 1, // 1-进行中 2-已结束 3-未发布
  remark: ''
})

// 表单验证规则
const questionnaireRules = {
  title: [
    { required: true, message: '请输入问卷标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择问卷类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入问卷描述', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}


// 类型和状态映射
const typeMap = {
  reading: { tag: 'success', text: '阅读调查' },
  feedback: { tag: 'warning', text: '用户反馈' },
  experience: { tag: 'info', text: '功能体验' },
  other: { tag: 'primary', text: '其他' }
}

const statusMap = {
  draft: { tag: 'info', text: '草稿' },
  published: { tag: 'success', text: '已发布' },
  closed: { tag: 'danger', text: '已关闭' }
}

const getTypeTag = (type) => typeMap[type]?.tag || 'primary'
const getTypeText = (type) => typeMap[type]?.text || type

const getStatusTag = (status) => statusMap[status]?.tag || 'info'
const getStatusText = (status) => statusMap[status]?.text || status

// 原始数据备份（用于某些操作，从API加载后更新）
const originalData = ref([])

// 搜索和重置
const handleSearch = async () => {
    pagination.currentPage = 1
  await loadQuestionnaireList()
  ElMessage.success('搜索完成')
}

const handleReset = () => {
  // 重置搜索表单
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  
  pagination.currentPage = 1
  loadQuestionnaireList()
  ElMessage.success('已重置搜索条件')
}

// 新建问卷
const handleCreate = () => {
  editType.value = 'create'
  editDialogVisible.value = true
  resetForm()
}

// 编辑问卷
const handleEdit = async (row) => {
  editType.value = 'edit'
  editDialogVisible.value = true
  
  // 填充表单数据
  questionnaireForm.id = row.id
  questionnaireForm.questId = row.questId
  questionnaireForm.title = row.title || ''
  questionnaireForm.type = row.type || 'reading' // 使用数据库中的类型
  questionnaireForm.description = row.description || ''
  questionnaireForm.surveyUrl = row.surveyUrl || ''
  questionnaireForm.status = row.status === 'published' ? 1 : row.status === 'closed' ? 2 : 3
  questionnaireForm.remark = row.remark || ''
}

// 重置表单
const resetForm = () => {
  questionnaireForm.title = ''
  questionnaireForm.type = ''
  questionnaireForm.description = ''
  // 重置问卷星字段
  questionnaireForm.questId = null
  questionnaireForm.surveyUrl = ''
  questionnaireForm.status = 1
  questionnaireForm.remark = ''
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!questionnaireFormRef.value) return
  
  try {
    await questionnaireFormRef.value.validate()
    
    // 验证问卷星必填字段
    if (!questionnaireForm.questId) {
      ElMessage.warning('请填写问卷星问卷ID')
      return
    }
    
    loading.value = true
    
    // 构建问卷星数据
    const surveyStarData = {
      questId: questionnaireForm.questId,
      title: questionnaireForm.title,
      description: questionnaireForm.description,
      type: questionnaireForm.type || 'reading', // 添加类型字段
      status: questionnaireForm.status || 3, // 3-未发布
      surveyUrl: questionnaireForm.surveyUrl,
      remark: questionnaireForm.remark || '通过后台管理系统创建',
      answerCount: 0
    }
    
    // 注意：响应拦截器已经处理了 code，成功时返回字符串消息，失败时抛出异常
    if (editType.value === 'create') {
      // 创建新问卷
      await createSurveyStar(surveyStarData)
    } else {
      // 更新问卷
      await updateSurveyStar(questionnaireForm.id, surveyStarData)
    }
    
    ElMessage.success('问卷保存成功')
    editDialogVisible.value = false
    // 刷新列表
    await handleSearch()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败：' + (error.message || '请检查表单内容'))
  } finally {
    loading.value = false
  }
}

// 保存并发布
const handleSaveAndPublish = async () => {
  if (!questionnaireFormRef.value) return
  
  try {
    await questionnaireFormRef.value.validate()
    
    // 验证问卷星必填字段
    if (!questionnaireForm.questId) {
      ElMessage.warning('请填写问卷星问卷ID')
      return
    }
    
    loading.value = true
    
    // 构建问卷星数据
    const surveyStarData = {
      questId: questionnaireForm.questId,
      title: questionnaireForm.title,
      description: questionnaireForm.description,
      type: questionnaireForm.type || 'reading', // 添加类型字段
      status: 1, // 1-进行中（发布状态）
      surveyUrl: questionnaireForm.surveyUrl,
      remark: questionnaireForm.remark || '通过后台管理系统创建并发布',
      answerCount: 0
    }
    
    // 注意：响应拦截器已经返回了 data 部分，成功时返回字符串消息，失败时抛出异常
    if (editType.value === 'create') {
      // 创建并发布新问卷
      await createSurveyStar(surveyStarData)
    } else {
      // 更新并发布问卷
      await updateSurveyStar(questionnaireForm.id, surveyStarData)
    }
    
    ElMessage.success('问卷发布成功！用户现在可以填写该问卷')
    editDialogVisible.value = false
    // 刷新列表
    await handleSearch()
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败：' + (error.message || '请检查表单内容'))
  } finally {
    loading.value = false
  }
}

// 打开问卷星帮助
const openSurveyStarHelp = () => {
  ElMessageBox.alert(
    `
    <div style="text-align: left; line-height: 1.8;">
      <h3 style="margin-top: 0;">如何获取问卷星报告ID？</h3>
      <p style="color: #E6A23C; font-size: 13px; margin-bottom: 12px;">
        <strong>⚠️ 重要：报告ID ≠ 问卷ID，无法从填写链接获取！</strong>
      </p>
      <h4 style="margin-top: 16px; margin-bottom: 8px;">获取步骤：</h4>
      <ol style="margin-left: 20px;">
        <li>登录问卷星网站：<a href="https://www.wjx.cn" target="_blank">https://www.wjx.cn</a></li>
        <li>进入"我的问卷"，找到您创建的问卷</li>
        <li><strong>点击"分析&下载"按钮</strong>（不是问卷标题）</li>
        <li>查看浏览器地址栏，URL格式为：<br/>
            <code style="background: #f5f5f5; padding: 2px 6px; border-radius: 3px;">https://www.wjx.cn/report/{报告ID}.aspx</code>
        </li>
        <li>将URL中的<strong>报告ID</strong>（数字）复制下来，填写到"问卷星报告ID"字段</li>
      </ol>
      <div style="background: #f0f9ff; border-left: 4px solid #409eff; padding: 12px; margin-top: 16px;">
        <p style="margin: 0 0 8px 0;"><strong>示例：</strong></p>
        <p style="margin: 0; font-size: 12px;">
          如果报告页面URL是：<code>https://www.wjx.cn/report/335617250.aspx</code><br/>
          那么报告ID就是：<strong>335617250</strong>
        </p>
      </div>
      <div style="background: #fff7e6; border-left: 4px solid #E6A23C; padding: 12px; margin-top: 12px;">
        <p style="margin: 0 0 8px 0;"><strong>⚠️ 常见错误：</strong></p>
        <ul style="margin: 0; padding-left: 20px; font-size: 12px;">
          <li>不要使用填写链接中的ID（如：<code>https://www.wjx.cn/vm/hgONdRo.aspx</code>）</li>
          <li>不要使用问卷列表中的问卷ID</li>
          <li>必须从"分析&下载"页面的URL中获取报告ID</li>
        </ul>
      </div>
    </div>
    `,
    '问卷星报告ID获取帮助',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '我知道了',
      width: '600px'
    }
  )
}

// 测试问卷链接
const testSurveyLink = () => {
  if (!questionnaireForm.surveyUrl) {
    ElMessage.warning('请先填写问卷链接')
    return
  }
  
  // 在新窗口打开链接
  window.open(questionnaireForm.surveyUrl, '_blank')
  ElMessage.success('已在新窗口打开问卷链接')
}

// 处理查看数据命令
const handleViewData = async (command, row) => {
  if (command === 'users') {
    // 查看用户填写记录
    await handleViewUserAnswers(row)
  } else if (command === 'wenjuanxing') {
    // 前往问卷星
    handleViewInWenjuanxing(row)
  }
}

// 查看用户填写记录
const handleViewUserAnswers = async (row) => {
  console.log('查看用户填写记录 - 问卷信息:', row)
  
  currentQuestId.value = row.questId
  userAnswersDialogVisible.value = true
  userAnswersList.value = []
  
  if (!row.questId) {
    ElMessage.warning('该问卷未设置问卷ID，无法查看填写记录')
    return
  }
  
  try {
    answersLoading.value = true
    
    // 调用API获取填写记录
    const response = await getSurveyStarAnswers(row.questId, {
      current: 1,
      size: 100
    })
    
    console.log('填写记录响应:', response)
    
    if (response && response.records) {
      userAnswersList.value = response.records.map(item => ({
        userId: item.userId || '未知用户',
        submitTime: item.submitTime,
        submitIp: item.submitIp || '-',
        answerId: item.answerId,
        status: item.status || 1
      }))
      
      ElMessage.success(`已加载 ${userAnswersList.value.length} 条填写记录`)
    } else {
      userAnswersList.value = []
      ElMessage.info('暂无填写记录')
    }
  } catch (error) {
    console.error('获取填写记录失败:', error)
    ElMessage.error('获取填写记录失败：' + (error.message || '请重试'))
    userAnswersList.value = []
  } finally {
    answersLoading.value = false
  }
}

// 在问卷星网站查看详细数据
const handleViewInWenjuanxing = (row) => {
  console.log('查看数据 - 问卷信息:', row)
  
  // 注意：问卷星的报告ID和问卷ID可能不同
  // 报告链接格式：https://www.wjx.cn/report/{报告ID}.aspx
  // 问卷链接格式：https://www.wjx.cn/vm/{code}.aspx 或 https://www.wjx.cn/jq/{问卷ID}.aspx
  
  let reportId = row.questId // 尝试使用 questId 作为报告ID
  
  // 如果 questId 不存在或为0，提示用户需要设置报告ID
  if (!reportId || reportId === 0) {
    ElMessageBox.alert(
      `该问卷的问卷星报告ID未设置。\n\n` +
      `请按以下步骤获取报告ID：\n` +
      `1. 登录问卷星网站：https://www.wjx.cn\n` +
      `2. 进入"我的问卷"，找到"${row.title}"\n` +
      `3. 点击"分析&下载"，查看浏览器地址栏\n` +
      `4. URL格式为：https://www.wjx.cn/report/{报告ID}.aspx\n` +
      `5. 将"报告ID"记录下来，在编辑问卷时填写到"问卷星问卷ID"字段\n\n` +
      `注意：报告ID和问卷ID可能不同，请使用报告页面的ID。`,
      '报告ID未设置',
      {
        confirmButtonText: '我知道了',
        type: 'warning'
      }
    )
    
    // 如果提供了 surveyUrl，可以打开问卷星网站
    if (row.surveyUrl) {
      ElMessageBox.confirm(
        '是否打开问卷星网站，您可以在那里查找该问卷并查看数据？',
        '打开问卷星',
        {
          confirmButtonText: '打开',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).then(() => {
        window.open('https://www.wjx.cn', '_blank')
      }).catch(() => {
        // 用户取消
      })
    }
      return
    }
  
  // 构建问卷星分析报告URL
  // 问卷星的分析报告URL格式：https://www.wjx.cn/report/{报告ID}.aspx
  const reportUrl = `https://www.wjx.cn/report/${reportId}.aspx`
  
  console.log('问卷星分析报告URL:', reportUrl)
  console.log('使用的报告ID:', reportId)
  
  ElMessageBox.confirm(
    `将在新窗口打开问卷星网站查看详细数据统计。\n\n问卷标题：${row.title}\n报告ID：${reportId}\n当前回收数：${row.responseCount || 0}份`,
    '查看问卷数据',
    {
      confirmButtonText: '前往问卷星',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    // 在新窗口打开问卷星分析页面
    window.open(reportUrl, '_blank')
  }).catch(() => {
    // 用户取消
  })
}

// 切换状态
const handleToggleStatus = async (row) => {
  const action = row.status === 'published' ? '关闭' : '发布'
  const actionType = row.status === 'published' ? 'warning' : 'success'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}该问卷吗？${action === '发布' ? '发布后用户即可开始填写' : '关闭后用户将无法继续填写'}`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: actionType
      }
    )
    
    loading.value = true
    
    // 计算新状态：1-进行中 2-已结束 3-未发布
    // 如果当前是published(进行中)，关闭后变为2(已结束)
    // 如果当前是closed(已结束)或draft(未发布)，发布后变为1(进行中)
    let newStatus
    if (row.status === 'published') {
      // 关闭：进行中 -> 已结束
      newStatus = 2
    } else {
      // 发布：已结束或未发布 -> 进行中
      newStatus = 1
    }
    
    console.log(`🔄 切换状态：问卷ID=${row.id}, 当前状态=${row.status}, 新状态=${newStatus}`)
    
    // 调用API更新状态
    // 注意：响应拦截器已经处理了 code，成功时返回字符串消息，失败时抛出异常
    await updateSurveyStarStatus(row.id, newStatus)
    
    // 刷新列表
    await loadQuestionnaireList()
    ElMessage.success(`${action}成功！`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态切换失败:', error)
      ElMessage.error(`${action}失败：${error.message || '请重试'}`)
    } else {
      ElMessage.info('已取消操作')
    }
  } finally {
    loading.value = false
  }
}

// 删除问卷
const handleDelete = async (row) => {
  // 检查是否有回收数据
  if (row.responseCount > 0) {
    try {
      await ElMessageBox.confirm(
        `该问卷已有 ${row.responseCount} 份回收数据，删除后将无法恢复！确定要删除吗？`,
        '警告',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'error'
        }
      )
    } catch {
      ElMessage.info('已取消删除')
      return
    }
  } else {
    try {
      await ElMessageBox.confirm(
        '确定要删除该问卷吗？删除后不可恢复！',
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      ElMessage.info('已取消删除')
      return
    }
  }
  
  try {
    loading.value = true
    
    // 调用API删除
    // 注意：响应拦截器已经处理了 code，成功时返回字符串消息，失败时抛出异常
    await deleteSurveyStar(row.id)
    
    // 刷新列表
    await loadQuestionnaireList()
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败：' + (error.message || '请重试'))
  } finally {
    loading.value = false
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (!selectedQuestionnaires.value.length) {
    ElMessage.warning('请选择要删除的问卷')
    return
  }

  // 检查是否有问卷包含回收数据
  const questionnairesWithData = selectedQuestionnaires.value.filter(q => q.responseCount > 0)
  const totalResponses = questionnairesWithData.reduce((sum, q) => sum + q.responseCount, 0)
  
  let confirmMessage = `确定要删除选中的 ${selectedQuestionnaires.value.length} 个问卷吗？`
  if (questionnairesWithData.length > 0) {
    confirmMessage += `\n其中 ${questionnairesWithData.length} 个问卷包含共 ${totalResponses} 份回收数据，删除后将无法恢复！`
  }

  try {
    await ElMessageBox.confirm(
      confirmMessage,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )
    
    loading.value = true
    
    const selectedIds = selectedQuestionnaires.value.map(row => row.id)
    
    // 调用API批量删除
    // 注意：响应拦截器已经返回了 data 部分，所以 response 就是包含 deletedCount 的对象
    const response = await batchDeleteSurveyStar(selectedIds)
    
    // 清空选择
    selectedQuestionnaires.value = []
    
    // 刷新列表
    await loadQuestionnaireList()
    
    ElMessage.success(`批量删除成功，共删除 ${response?.deletedCount || selectedIds.length} 个问卷`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败：' + (error.message || '请重试'))
    } else {
      ElMessage.info('已取消删除')
    }
  } finally {
    loading.value = false
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedQuestionnaires.value = selection
}

// 更新统计数据
const updateStats = async () => {
  try {
    // 从API获取真实统计数据
    // 注意：响应拦截器已经返回了 data 部分，所以 response 就是统计数据 Map
    const response = await getSurveyStarStatistics()
    
    if (response && typeof response === 'object') {
      stats.value.totalQuestionnaires = response.totalQuestionnaires || 0
      stats.value.publishedQuestionnaires = response.publishedQuestionnaires || 0
      stats.value.totalResponses = response.totalResponses || 0
    } else {
      // 数据格式不正确，从列表数据计算
      calculateStatsFromList()
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // API调用失败，从列表数据计算统计数据（降级方案）
    calculateStatsFromList()
  }
}

// 从列表数据计算统计数据（降级方案）
const calculateStatsFromList = () => {
  const data = questionnaireList.value || []
  stats.value.totalQuestionnaires = data.length
  stats.value.publishedQuestionnaires = data.filter(q => q.status === 'published').length
  stats.value.totalResponses = data.reduce((sum, q) => sum + (q.responseCount || 0), 0)
}

// 分页
const handleSizeChange = async (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  await loadQuestionnaireList()
}

const handleCurrentChange = async (val) => {
  pagination.currentPage = val
  await loadQuestionnaireList()
}


// 复制问卷功能
const handleCopyQuestionnaire = async (row) => {
  try {
    loading.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    const copiedQuestionnaire = {
      ...row,
      id: Date.now(),
      title: `${row.title} - 副本`,
      status: 'draft',
      responseCount: 0,
      createTime: new Date().toLocaleString(),
      publishTime: null
    }
    
    originalData.value.unshift(copiedQuestionnaire)
    questionnaireList.value.unshift(copiedQuestionnaire)
    
    updateStats()
    
    ElMessage.success('问卷复制成功')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请重试')
  } finally {
    loading.value = false
  }
}

// 预览问卷功能
const handlePreviewQuestionnaire = (row) => {
  // 这里可以打开新窗口或对话框来预览问卷
  ElMessage.info('预览功能开发中，将打开问卷预览页面')
  
  // 模拟打开预览页面
  const previewUrl = `/questionnaire/preview/${row.id}`
  console.log('预览链接:', previewUrl)
}

// 加载问卷列表
const loadQuestionnaireList = async () => {
  loading.value = true
  try {
    // 注意：响应拦截器已经返回了 data 部分，所以 response 就是 IPage 对象
    const response = await getSurveyStarList({
      current: pagination.currentPage,
      size: pagination.pageSize,
      title: searchForm.title,
      status: searchForm.status ? (searchForm.status === 'published' ? 1 : searchForm.status === 'closed' ? 2 : 3) : null,
      type: searchForm.type || null
    })
    
    // 响应拦截器已经处理了 code，这里 response 就是 IPage 对象
    if (response && response.records) {
      // 转换数据格式以适配现有表格
      questionnaireList.value = response.records.map(item => ({
        id: item.id,
        questId: item.questId,
        title: item.title,
        type: item.type || 'reading', // 使用数据库中的类型，默认为reading
        description: item.description,
        questionCount: 0, // 问卷星不提供题目数
        responseCount: item.answerCount || 0,
        status: item.status === 1 ? 'published' : item.status === 2 ? 'closed' : 'draft',
        createTime: item.createdAt ? new Date(item.createdAt).toLocaleString() : '',
        publishTime: item.status === 1 ? (item.updatedAt ? new Date(item.updatedAt).toLocaleString() : '') : null,
        startTime: null,
        endTime: null,
        surveyUrl: item.surveyUrl,
        remark: item.remark
      }))
      
      pagination.total = response.total || 0
      originalData.value = [...questionnaireList.value]
      
      // 更新统计数据
      await updateStats()
    } else {
      // 数据格式不正确
      console.warn('加载问卷列表失败，响应数据格式不正确:', response)
      questionnaireList.value = []
      pagination.total = 0
      ElMessage.warning('加载问卷列表失败：数据格式不正确')
    }
  } catch (error) {
    console.error('加载问卷列表失败:', error)
    // API调用失败，清空列表，避免显示旧数据
    questionnaireList.value = []
    pagination.total = 0
    ElMessage.error('加载问卷列表失败：' + (error.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  // 加载问卷列表
  loadQuestionnaireList()
  
  // 加载统计数据
  updateStats()
  
  console.log('问卷管理系统初始化完成')
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
  background-color: #f5f7fa;

  .search-card {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);

    .search-form {
      margin-bottom: 0;
    }

    .operation-container {
      display: flex;
      gap: 16px;
      margin-top: 16px;
    }
  }

  .stats-container {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .stat-card {
      background: #fff;
      padding: 20px;
      border-radius: 4px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        width: 48px;
        height: 48px;
        background: #409eff;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 20px;
      }

      .stat-content {
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .table-card {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);

    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

:deep(.el-dialog__footer) {
  padding-top: 20px;
  text-align: right;
}

.response-stats {
  .stats-overview {
    margin-bottom: 24px;

    .stat-item {
      text-align: center;
      padding: 16px;
      background-color: #f8f9fa;
      border-radius: 8px;

      .stat-number {
        font-size: 32px;
        font-weight: 600;
        color: #409eff;
        margin-bottom: 8px;
      }

      .stat-text {
        font-size: 14px;
        color: #606266;
      }
    }
  }

  .response-details {
    h4 {
      margin-bottom: 16px;
      color: #303133;
    }

    .detail-stat-item {
      text-align: center;
      padding: 20px;
      background-color: #f8f9fa;
      border-radius: 8px;
      border: 1px solid #ebeef5;

      .stat-title {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #409eff;
      }
    }

    .device-stats {
      .device-list {
        .device-item {
          display: flex;
          align-items: center;
          margin-bottom: 16px;
          gap: 12px;

          .device-name {
            width: 60px;
            font-size: 14px;
            color: #606266;
          }

          .device-bar {
            flex: 1;
            height: 20px;
            background-color: #f0f2f5;
            border-radius: 10px;
            overflow: hidden;

            .bar-fill {
              height: 100%;
              background: linear-gradient(to right, #409eff, #67c23a);
              transition: width 0.3s ease;
            }
          }

          .device-percent {
            width: 40px;
            text-align: right;
            font-size: 14px;
            color: #409eff;
            font-weight: 600;
          }
        }
      }
    }

    .question-stats {
      .question-stat-item {
        margin-bottom: 24px;
        padding: 16px;
        background-color: #fafafa;
        border-radius: 8px;
        border: 1px solid #ebeef5;

        h5 {
          margin: 0 0 12px 0;
          color: #303133;
          font-size: 16px;
        }

        .stat-summary {
          margin-bottom: 16px;
          font-size: 12px;
          color: #909399;

          span {
            margin-right: 16px;
          }
        }

        .options-stat {
          .option-stat {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            gap: 12px;

            .option-text {
              width: 100px;
              font-size: 14px;
              color: #606266;
            }

            .option-bar {
              flex: 1;
              height: 16px;
              background-color: #f0f2f5;
              border-radius: 8px;
              overflow: hidden;

              .bar-fill {
                height: 100%;
                background: linear-gradient(to right, #e6a23c, #f56c6c);
                transition: width 0.3s ease;
              }
            }

            .option-count {
              width: 50px;
              text-align: right;
              font-size: 12px;
              color: #606266;
            }
          }
        }
      }
    }

    .export-actions {
      margin-top: 24px;
      text-align: center;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;
    }
  }
}
</style>