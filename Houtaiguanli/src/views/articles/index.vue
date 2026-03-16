<template>
  <div class="page-container">
    <!-- 搜索卡片 -->
    <div class="search-card">
      <el-form :inline="true" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="filterForm.title" placeholder="请输入文章标题" clearable style="width: 200px" />
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
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增文章</el-button>
        <el-button type="danger" :icon="Delete" :disabled="!selectedArticles.length" @click="handleBatchDelete">批量删除</el-button>
      </div>
    </div>

    <!-- 表格卡片 -->
    <div class="table-card">
      <el-table v-loading="loading" :data="articleList" style="width: 100%" @selection-change="handleSelectionChange">
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
        <el-table-column prop="viewCount" label="阅读量" width="80" align="center" />
        <el-table-column prop="likeCount" label="点赞数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
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
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增文章' : '编辑文章'" width="70%" center @close="handleDialogClose" class="article-dialog">
      <el-form ref="articleFormRef" :model="articleForm" :rules="articleRules" label-width="80px">
        <el-form-item label="封面图" prop="coverUrl">
          <div style="display: flex; align-items: center; gap: 16px;">
            <el-image v-if="articleForm.coverUrl" :src="articleForm.coverUrl" style="width: 120px; height: 90px; border-radius: 4px" fit="cover" />
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

        <el-form-item label="标题" prop="title">
          <el-input v-model="articleForm.title" placeholder="请输入文章标题" />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="articleForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content" class="editor-form-item">
          <div class="editor-container">
            <Editor
              :api-key="tinymceConfig.apiKey"
              :init="tinymceConfig.init"
              v-model="articleForm.content"
            />
          </div>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="articleForm.status">
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
import Editor from '@tinymce/tinymce-vue'
import { getArticleList, addArticle, updateArticle, deleteArticle, getCategoryList, uploadFile, batchDeleteArticles } from '@/api/article'

const filterForm = reactive({ title: '', categoryId: '', status: '' })
const loading = ref(false)
const submitting = ref(false)
const articleList = ref([])
const selectedArticles = ref([])
const pagination = reactive({ currentPage: 1, pageSize: 20, total: 0 })
const categories = ref([])
const dialogVisible = ref(false)
const dialogType = ref('add')
const articleFormRef = ref(null)
const articleForm = reactive({ 
  id: null, 
  title: '', 
  content: '', 
  categoryId: '', 
  status: 1, 
  coverUrl: '',
  tempImages: [] 
})

const articleRules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择文章分类', trigger: 'change' }],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' },
    { min: 10, message: '内容不能少于 10 个字符', trigger: 'blur' }
  ],
  coverUrl: [{ required: true, message: '请上传文章封面图' }]
}

// TinyMCE 配置 - 完全参照 content-manage.vue
const tinymceConfig = reactive({
  apiKey: 'j3qh45h62jopjn7ikfhrdup0xmmyftmgs7116xswbcs2t29u',
  init: {
    height: 500,
    toolbar_mode: 'sliding',
    plugins: [
      'anchor', 'autolink', 'charmap', 'codesample', 'emoticons', 'link', 'lists', 'media', 
      'searchreplace', 'table', 'visualblocks', 'wordcount',
      'checklist', 'mediaembed', 'casechange', 'formatpainter', 'pageembed', 'a11ychecker',
      'permanentpen', 'powerpaste', 'advtable', 'advcode', 'advtemplate',
      'mentions', 'tinycomments', 'tableofcontents', 'footnotes', 'mergetags', 'autocorrect',
      'typography', 'inlinecss', 'markdown'
    ],
    toolbar: [
      'undo redo | blocks fontfamily fontsize',
      'bold italic underline strikethrough | link image media table mergetags | align lineheight',
      'checklist numlist bullist indent outdent | emoticons charmap | removeformat',
      'forecolor backcolor | formatpainter removeformat | pagebreak | charmap emoticons | preview print | insertfile image media pageembed template link anchor codesample | ltr rtl'
    ].join(' | '),
    content_style: `
      body {
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
        font-size: 16px;
        line-height: 1.6;
        color: #333;
        max-width: 100%;
        margin: 0 auto;
        padding: 20px;
      }
      img {
        max-width: 100%;
        height: auto;
      }
    `,
    images_upload_handler: (blobInfo, success, failure) => {
      console.log('图片插入处理开始，保存临时数据等待发布时上传')
      try {
        const file = blobInfo.blob()
        const fileName = blobInfo.filename() || `image_${Date.now()}.jpg`
        const tempUrl = URL.createObjectURL(file)
        
        if (!articleForm.tempImages) {
          articleForm.tempImages = []
        }
        
        const tempImageInfo = {
          file: file,
          tempUrl: tempUrl,
          fileName: fileName,
          id: `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        }
        
        articleForm.tempImages.push(tempImageInfo)
        console.log('临时图片已保存:', { fileName, tempUrl, id: tempImageInfo.id })
        success(tempUrl)
      } catch (error) {
        console.error('处理图片失败:', error)
        if (failure && typeof failure === 'function') {
          failure('处理图片失败: ' + (error.message || error))
        }
      }
    },
    browser_spellcheck: false,
    language: 'zh_CN',
    language_url: 'https://cdn.tiny.cloud/1/j3qh45h62jopjn7ikfhrdup0xmmyftmgs7116xswbcs2t29u/tinymce/6/langs/zh_CN.js',
    autosave_interval: '30s',
    autosave_prefix: 'tinymce-autosave-{path}-{id}-',
    branding: false,
    menubar: true,
    statusbar: true,
    resize: true,
    paste_data_images: true,
    image_advtab: true,
    image_title: true,
    automatic_uploads: false,
    file_picker_types: 'image',
    tinycomments_mode: 'embedded',
    tinycomments_author: 'Author name',
    mergetags_list: [
      { value: 'First.Name', title: 'First Name' },
      { value: 'Email', title: 'Email' }
    ]
  }
})

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

const fetchArticleList = async () => {
  loading.value = true
  try {
    const params = { current: pagination.currentPage, size: pagination.pageSize }
    if (filterForm.title) params.title = filterForm.title
    if (filterForm.categoryId) params.categoryId = filterForm.categoryId
    if (filterForm.status !== '') params.status = filterForm.status
    const response = await getArticleList(params)
    if (response && response.records) {
      articleList.value = response.records
      pagination.total = response.total
    } else if (Array.isArray(response)) {
      articleList.value = response
      pagination.total = response.length
    }
  } catch (error) {
    console.error('获取文章列表失败:', error)
    ElMessage.error('获取文章列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchArticleList()
}

const handleReset = () => {
  filterForm.title = ''
  filterForm.categoryId = ''
  filterForm.status = ''
  pagination.currentPage = 1
  fetchArticleList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchArticleList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchArticleList()
}

const handleCoverSelect = async (file) => {
  try {
    const response = await uploadFile(file.file, { type: 'image', path: 'article/cover' })
    if (response && response.originUrl) {
      articleForm.coverUrl = response.originUrl
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

const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  if (articleFormRef.value) articleFormRef.value.resetFields()
  articleForm.id = null
  articleForm.title = ''
  articleForm.content = ''
  articleForm.categoryId = ''
  articleForm.status = 1
  articleForm.coverUrl = ''
  articleForm.tempImages = []
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  articleForm.id = row.id
  articleForm.title = row.title
  articleForm.content = row.content
  articleForm.categoryId = row.categoryId
  articleForm.status = row.status
  articleForm.coverUrl = row.coverUrl || ''
  articleForm.tempImages = []
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该文章吗？', '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      try {
        loading.value = true
        await deleteArticle(row.id)
        ElMessage.success('删除成功')
        fetchArticleList()
      } catch (error) {
        console.error('删除文章失败:', error)
        ElMessage.error('删除文章失败：' + error.message)
      } finally {
        loading.value = false
      }
    })
    .catch(() => { ElMessage.info('已取消删除') })
}

const handleBatchDelete = () => {
  if (!selectedArticles.value.length) {
    ElMessage.warning('请先选择要删除的文章')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedArticles.value.length} 篇文章吗？`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      try {
        loading.value = true
        const ids = selectedArticles.value.map(item => item.id)
        await batchDeleteArticles(ids)
        ElMessage.success('批量删除成功')
        fetchArticleList()
        selectedArticles.value = []
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
  selectedArticles.value = rows
}

// 上传临时图片
const uploadTempImages = async (articleId) => {
  if (!articleForm.tempImages || articleForm.tempImages.length === 0) {
    return articleForm.content
  }

  let updatedContent = articleForm.content
  for (let i = 0; i < articleForm.tempImages.length; i++) {
    const tempImage = articleForm.tempImages[i]
    try {
      const response = await uploadFile(tempImage.file, { 
        type: 'image', 
        path: `article/${articleId}`,
        fileName: `article_image_${i + 1}.jpg`
      })
      
      if (response && response.originUrl) {
        updatedContent = updatedContent.replace(tempImage.tempUrl, response.originUrl)
        console.log(`第 ${i + 1} 张图片上传成功:`, response.originUrl)
      }
    } catch (error) {
      console.error(`第 ${i + 1} 张图片上传失败:`, error)
    }
  }
  return updatedContent
}

const handleSubmit = async () => {
  if (!articleFormRef.value) return
  try {
    await articleFormRef.value.validate()
    submitting.value = true

    let articleId = articleForm.id
    let submittedContent = articleForm.content

    // 如果是新增，先创建文章获取ID
    if (!articleId) {
      const createRes = await addArticle({
        title: articleForm.title,
        content: '',
        categoryId: parseInt(articleForm.categoryId),
        status: articleForm.status,
        coverUrl: articleForm.coverUrl,
        type: 1
      })
      articleId = createRes.id || createRes.data?.id
      articleForm.id = articleId
    }

    // 上传临时图片并替换内容中的URL
    submittedContent = await uploadTempImages(articleId)

    // 提交最终数据
    const submitData = {
      title: articleForm.title,
      content: submittedContent,
      categoryId: parseInt(articleForm.categoryId),
      status: articleForm.status,
      coverUrl: articleForm.coverUrl,
      type: 1
    }

    if (dialogType.value === 'add') {
      await updateArticle(articleId, submitData)
      ElMessage.success('添加成功')
    } else {
      await updateArticle(articleId, submitData)
      ElMessage.success('编辑成功')
    }

    dialogVisible.value = false
    fetchArticleList()
  } catch (error) {
    console.error('提交失败:', error)
    if (error.message) ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

const handleDialogClose = () => {
  // 清理临时图片的blob URL，避免内存泄露
  if (articleForm.tempImages && articleForm.tempImages.length > 0) {
    articleForm.tempImages.forEach(tempImage => {
      if (tempImage.tempUrl) {
        URL.revokeObjectURL(tempImage.tempUrl)
      }
    })
  }
  articleForm.tempImages = []
}

onMounted(() => {
  fetchCategories()
  fetchArticleList()
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

:deep(.tox-tinymce) {
  border: 1px solid #dcdfe0 !important;
  border-radius: 4px !important;
}

:deep(.tox-statusbar) {
  border-top: 1px solid #dcdfe0 !important;
}

/* 文章对话框样式优化 */
:deep(.article-dialog) {
  .el-dialog {
    max-height: 90vh;
    display: flex;
    flex-direction: column;
  }
  
  .el-dialog__body {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
  }
  
  .el-form {
    margin: 0;
  }
}

.editor-form-item {
  :deep(.el-form__content) {
    line-height: initial;
  }
}

.editor-container {
  width: 100%;
  
  :deep(.tox-tinymce) {
    height: 300px !important;
    max-width: 100%;
  }
  
  :deep(.tox-editor-container) {
    border-radius: 4px;
  }
}
</style>
