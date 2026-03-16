<template>
  <div class="page-container">
    <!-- 搜索卡片 -->
    <div class="search-card">
      <el-form :inline="true" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="filterForm.title" placeholder="请输入视频标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filterForm.categoryId" placeholder="请选择分类" clearable style="width: 200px">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="operation-container">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增视频</el-button>
        <el-button type="danger" :icon="Delete" :disabled="!selectedVideos.length" @click="handleBatchDelete">批量删除</el-button>
      </div>
    </div>

    <!-- 表格卡片 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="videoList" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="封面" width="100" align="center">
          <template #default="scope">
            <el-image v-if="scope.row.coverUrl" :src="scope.row.coverUrl" style="width: 80px; height: 60px" fit="cover" :preview-src-list="[scope.row.coverUrl]" />
            <div v-else style="width: 80px; height: 60px; background: #f5f5f5; display: flex; align-items: center; justify-content: center;">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="分类" width="100" align="center">
          <template #default="scope">{{ getCategoryName(scope.row.categoryId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="scope">{{ formatDate(scope.row.createdTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增视频' : '编辑视频'" width="60%" center @close="handleDialogClose">
      <el-form ref="videoFormRef" :model="videoForm" :rules="videoRules" label-width="100px">
        <el-form-item label="封面图" prop="coverUrl">
          <div style="display: flex; align-items: center; gap: 16px;">
            <el-image v-if="videoForm.coverUrl" :src="videoForm.coverUrl" style="width: 120px; height: 90px; border-radius: 4px" fit="cover" />
            <el-upload
              accept="image/*"
              auto-upload
              :http-request="handleCoverSelect"
              :show-file-list="false"
            >
              <el-button type="primary">上传封面</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="视频文件" prop="mediaUrl" v-if="dialogType === 'add'">
          <div style="display: flex; align-items: center; gap: 16px;">
            <div v-if="videoForm.mediaUrl" class="video-info">
              <span class="video-name">{{ videoFileName }}</span>
            </div>
            <el-upload
              accept="video/*"
              auto-upload
              :http-request="handleVideoSelect"
              :show-file-list="false"
            >
              <el-button type="primary">上传视频</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="videoForm.title" placeholder="请输入视频标题" />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="videoForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="简介" prop="content">
          <el-input 
            v-model="videoForm.content" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入视频简介"
            show-word-limit
            maxlength="500"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="videoForm.status">
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshRight, Plus, Delete, Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArticleList, uploadFile, getCategoryList } from '@/api/article'

const filterForm = reactive({ title: '', categoryId: '', status: '' })
const loading = ref(false)
const submitting = ref(false)
const videoList = ref([])
const selectedVideos = ref([])
const pagination = reactive({ currentPage: 1, pageSize: 20, total: 0 })
const categories = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const videoFormRef = ref(null)
const videoForm = reactive({ 
  id: null, 
  title: '', 
  content: '', 
  categoryId: '', 
  status: 1, 
  coverUrl: '',
  mediaUrl: '',
  tempImages: []
})
const videoFileName = ref('')

const videoRules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择视频分类', trigger: 'change' }],
  content: [
    { required: true, message: '请输入视频简介', trigger: 'blur' },
    { min: 10, message: '简介不能少于 10 个字符', trigger: 'blur' }
  ],
  coverUrl: [{ required: true, message: '请上传视频封面图' }],
  mediaUrl: [{ required: true, message: '请上传视频文件' }]
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const getCategoryName = (categoryId) => {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : '-'
}

const fetchCategories = async () => {
  try {
    const response = await getCategoryList()
    if (response && Array.isArray(response)) {
      categories.value = response
    } else if (response && response.records) {
      categories.value = response.records
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchVideoList = async () => {
  loading.value = true
  try {
    const params = { current: pagination.currentPage, size: pagination.pageSize, type: 2 }
    if (filterForm.title) params.title = filterForm.title
    if (filterForm.categoryId) params.categoryId = filterForm.categoryId
    if (filterForm.status !== '') params.status = filterForm.status
    
    const response = await getArticleList(params)
    if (response && response.records) {
      videoList.value = response.records
      pagination.total = response.total
    } else if (Array.isArray(response)) {
      videoList.value = response
      pagination.total = response.length
    }
  } catch (error) {
    console.error('获取视频列表失败:', error)
    ElMessage.error('获取视频列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchVideoList()
}

const handleReset = () => {
  filterForm.title = ''
  filterForm.categoryId = ''
  filterForm.status = ''
  pagination.currentPage = 1
  fetchVideoList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchVideoList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchVideoList()
}

const handleCoverSelect = async (file) => {
  try {
    const response = await uploadFile(file.file, { type: 'image', path: 'video/cover' })
    if (response && response.originUrl) {
      videoForm.coverUrl = response.originUrl
      ElMessage.success('封面图上传成功')
    } else {
      ElMessage.error('获取封面图URL失败')
    }
  } catch (error) {
    console.error('上传封面图失败:', error)
    ElMessage.error('上传封面图失败')
  }
  return { name: file.file.name, url: 'https://example.com/fake.jpg' }
}

const handleVideoSelect = async (file) => {
  try {
    const response = await uploadFile(file.file, { type: 'video', path: 'video/content' })
    if (response && response.originUrl) {
      videoForm.mediaUrl = response.originUrl
      videoFileName.value = file.file.name
      ElMessage.success('视频上传成功')
    } else {
      ElMessage.error('获取视频URL失败')
    }
  } catch (error) {
    console.error('上传视频失败:', error)
    ElMessage.error('上传视频失败')
  }
  return { name: file.file.name, url: 'https://example.com/fake.mp4' }
}

const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  if (videoFormRef.value) videoFormRef.value.resetFields()
  videoForm.id = null
  videoForm.title = ''
  videoForm.content = ''
  videoForm.categoryId = ''
  videoForm.status = 1
  videoForm.coverUrl = ''
  videoForm.mediaUrl = ''
  videoFileName.value = ''
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  videoForm.id = row.id
  videoForm.title = row.title
  videoForm.content = row.content
  videoForm.categoryId = row.categoryId
  videoForm.status = row.status
  videoForm.coverUrl = row.coverUrl || ''
  videoForm.mediaUrl = row.mediaUrl || ''
  videoFileName.value = row.mediaUrl ? row.mediaUrl.split('/').pop() : ''
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该视频吗？', '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      try {
        loading.value = true
        await fetch(`/api/content/${row.id}`, { method: 'DELETE' })
        ElMessage.success('删除成功')
        fetchVideoList()
      } catch (error) {
        console.error('删除视频失败:', error)
        ElMessage.error('删除视频失败：' + error.message)
      } finally {
        loading.value = false
      }
    })
    .catch(() => { ElMessage.info('已取消删除') })
}

const handleBatchDelete = () => {
  if (!selectedVideos.value.length) {
    ElMessage.warning('请先选择要删除的视频')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedVideos.value.length} 个视频吗？`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      try {
        loading.value = true
        const ids = selectedVideos.value.map(item => item.id)
        await fetch('/api/content/batch', { method: 'DELETE', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(ids) })
        ElMessage.success('批量删除成功')
        fetchVideoList()
        selectedVideos.value = []
      } catch (error) {
        console.error('批量删除失败:', error)
        ElMessage.error('批量删除失败：' + error.message)
      } finally {
        loading.value = false
      }
    })
    .catch(() => { ElMessage.info('已取消删除') })
}

const handleSelectionChange = (rows) => {
  selectedVideos.value = rows
}

const handleSubmit = async () => {
  if (!videoFormRef.value) return
  try {
    await videoFormRef.value.validate()
    submitting.value = true

    const submitData = {
      title: videoForm.title,
      content: videoForm.content,
      categoryId: parseInt(videoForm.categoryId),
      status: videoForm.status,
      coverUrl: videoForm.coverUrl,
      mediaUrl: videoForm.mediaUrl,
      type: 2
    }

    if (dialogType.value === 'add') {
      await fetch('/api/content', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(submitData) })
      ElMessage.success('添加成功')
    } else {
      await fetch(`/api/content/${videoForm.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(submitData) })
      ElMessage.success('编辑成功')
    }

    dialogVisible.value = false
    fetchVideoList()
  } catch (error) {
    console.error('提交失败:', error)
    if (error.message) ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

const handleDialogClose = () => {
  videoForm.tempImages = []
}

onMounted(() => {
  fetchCategories()
  fetchVideoList()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
  height: calc(100vh - 40px);
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;

  .search-card {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    flex-shrink: 0;

    .search-form {
      margin-bottom: 16px;
    }

    .operation-container {
      display: flex;
      justify-content: flex-start;
      gap: 16px;
    }
  }

  .table-card {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .el-table {
      flex: 1;
      overflow-y: auto;
    }
  }

  .pagination-container {
    margin-top: 16px;
    display: flex;
    justify-content: center;
    padding: 16px 0 0 0;
    border-top: 1px solid #f0f0f0;
    flex-shrink: 0;
  }
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

.video-info {
  padding: 8px 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
  font-size: 14px;

  .video-name {
    color: #333;
  }
}
</style>
