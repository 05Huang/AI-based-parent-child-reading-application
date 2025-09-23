<template>
  <view class="content-manage">
    <view class="form-container">
      <view class="form-item">
        <text class="label">标题</text>
        <input v-model="formData.title" placeholder="请输入文章标题" />
      </view>
      
      <view class="form-item">
        <text class="label">分类</text>
        <picker @change="onCategoryChange" :value="categoryIndex" :range="categories" range-key="name">
          <view class="picker">
            {{ categories[categoryIndex]?.name || '请选择分类' }}
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <text class="label">标签</text>
        <input v-model="formData.tags" placeholder="请输入标签，多个标签用逗号分隔" />
      </view>
      
      <view class="form-item">
        <text class="label">封面图</text>
        <view class="upload-container">
          <image v-if="tempCoverUrl" :src="tempCoverUrl" mode="aspectFit" class="preview-image" />
          <button @click="selectCover" class="upload-btn">{{ tempCoverUrl ? '更换封面' : '选择封面' }}</button>
        </view>
      </view>
      
      <view class="form-item">
        <text class="label">内容</text>
        <editor 
          id="editor"
          class="editor"
          placeholder="请输入文章内容"
          @ready="onEditorReady"
          @input="onEditorInput"
          @statuschange="onStatusChange"
        ></editor>
      </view>
      
      <button @click="submitContent" type="primary" class="submit-btn">发布文章</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, onMounted } from 'vue'

const { proxy } = getCurrentInstance()

// 响应式数据
const formData = reactive({
  title: '',
  content: '',
  type: 1, // 1-图文
  coverUrl: '',
  categoryId: null,
  tags: '',
  imageUrls: []
})

const editorCtx = ref(null)
const categories = ref([])
const categoryIndex = ref(0)
const tempCoverUrl = ref('') // 临时预览用的封面图URL
const tempCoverFile = ref(null) // 临时存储选择的文件

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await proxy.$http.get('/api/category/all')
    if(res.code === 200) {
      categories.value = res.data
      console.log('获取分类列表成功:', categories.value)
    }
  } catch(e) {
    console.error('获取分类列表失败:', e)
    uni.showToast({
      title: '获取分类列表失败',
      icon: 'none'
    })
  }
}

// 分类选择改变
const onCategoryChange = (e) => {
  categoryIndex.value = e.detail.value
  formData.categoryId = categories.value[categoryIndex.value].id
}

// 编辑器准备就绪
const onEditorReady = () => {
  console.log('编辑器准备就绪')
  uni.createSelectorQuery().select('#editor').context((res) => {
    editorCtx.value = res.context
  }).exec()
}

// 编辑器内容变化
const onEditorInput = (e) => {
  formData.content = e.detail.html
  console.log('编辑器内容:', formData.content)
}

// 编辑器状态变化
const onStatusChange = (e) => {
  console.log('编辑器状态:', e.detail)
}

// 选择封面图
const selectCover = async () => {
  try {
    // 选择图片
    const { tempFilePaths } = await new Promise((resolve, reject) => {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: resolve,
        fail: reject
      })
    })
    
    if (!tempFilePaths || !tempFilePaths.length) {
      throw new Error('未选择图片')
    }

    // 保存临时文件信息
    tempCoverFile.value = tempFilePaths[0]
    tempCoverUrl.value = tempFilePaths[0]
    
    uni.showToast({
      title: '已选择封面图',
      icon: 'success'
    })
  } catch(e) {
    console.error('选择封面图失败:', e)
    uni.showToast({
      title: e.message || '选择失败',
      icon: 'none'
    })
  }
}

// 上传封面图
const uploadCover = async (articleId) => {
  try {
    if (!tempCoverFile.value) {
      throw new Error('请先选择封面图')
    }

    // 上传图片
    const uploadRes = await new Promise((resolve, reject) => {
      uni.uploadFile({
        url: proxy.$http.baseUrl + '/api/file/upload',
        filePath: tempCoverFile.value,
        name: 'file',
        header: {
          'accessToken': uni.getStorageSync('token')
        },
        formData: {
          type: 'image',
          path: `article/${articleId}`,
          fileName: 'article_cover.jpg'
        },
        success: resolve,
        fail: reject
      })
    })
    
    console.log('文件上传响应:', uploadRes)
    const res = JSON.parse(uploadRes.data)
    console.log('解析后的响应:', res)
    
    if(res.code === 200) {
      const coverUrl = res.data.originUrl // 使用 originUrl 字段
      console.log('获取到的封面图URL:', coverUrl)
      return coverUrl
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch(e) {
    console.error('封面图上传失败:', e)
    throw e
  }
}

// 提交内容
const submitContent = async () => {
  if(!formData.title) {
    uni.showToast({
      title: '请输入标题',
      icon: 'none'
    })
    return
  }
  
  if(!formData.categoryId) {
    uni.showToast({
      title: '请选择分类',
      icon: 'none'
    })
    return
  }
  
  if(!tempCoverFile.value) {
    uni.showToast({
      title: '请选择封面图',
      icon: 'none'
    })
    return
  }
  
  if(!formData.content) {
    uni.showToast({
      title: '请输入内容',
      icon: 'none'
    })
    return
  }
  
  try {
    // 1. 先创建文章获取ID
    const createRes = await proxy.$http.post('/api/content', {
      ...formData,
      coverUrl: '' // 先不设置封面图URL
    })
    
    if(createRes.code !== 200) {
      throw new Error(createRes.message || '创建文章失败')
    }
    
    const articleId = createRes.data.id
    console.log('创建的文章ID:', articleId)
    
    // 2. 上传封面图
    const coverUrl = await uploadCover(articleId)
    console.log('封面图上传成功，准备更新文章，coverUrl:', coverUrl)
    
    // 3. 更新文章的封面图URL
    const updateData = {
      ...formData,
      coverUrl: coverUrl
    }
    console.log('更新文章的数据:', updateData)
    
    const updateRes = await proxy.$http.put(`/api/content/${articleId}`, updateData)
    console.log('更新文章响应:', updateRes)
    
    if(updateRes.code === 200) {
      uni.showToast({
        title: '发布成功',
        icon: 'success'
      })
      
      // 清空表单
      Object.assign(formData, {
        title: '',
        content: '',
        type: 1,
        coverUrl: '',
        categoryId: null,
        tags: '',
        imageUrls: []
      })
      tempCoverFile.value = null
      tempCoverUrl.value = ''
      if(editorCtx.value) {
        editorCtx.value.clear()
      }
    } else {
      throw new Error(updateRes.message || '更新封面图失败')
    }
  } catch(e) {
    console.error('发布失败:', e)
    uni.showToast({
      title: e.message || '发布失败',
      icon: 'none'
    })
  }
}

// 页面加载时获取分类列表
onMounted(() => {
  fetchCategories()
})
</script>

<style>
.content-manage {
  padding: 20px;
}

.form-container {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
}

.form-item {
  margin-bottom: 30px;
}

.label {
  display: block;
  margin-bottom: 10px;
  font-size: 14px;
  color: #333;
}

input {
  width: 100%;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 20px;
  font-size: 14px;
}

.picker {
  width: 100%;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 20px;
  font-size: 14px;
  line-height: 40px;
}

.upload-container {
  display: flex;
  align-items: center;
}

.preview-image {
  width: 100px;
  height: 100px;
  margin-right: 20px;
  border-radius: 8px;
}

.upload-btn {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
}

.editor {
  min-height: 250px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  line-height: 44px;
  font-size: 16px;
  margin-top: 40px;
}
</style> 