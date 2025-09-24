<template>
  <view class="video-manage">
    <view class="form-container">
      <view class="form-item">
        <text class="label">视频标题</text>
        <input v-model="formData.title" placeholder="请输入视频标题" />
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
        <text class="label">视频简介</text>
        <textarea 
          v-model="formData.content" 
          placeholder="请输入视频简介" 
          class="textarea-content"
          auto-height
          :maxlength="500"
        />
        <text class="char-count">{{ formData.content.length }}/500</text>
      </view>
      
      <view class="form-item">
        <text class="label">封面图</text>
        <view class="upload-container">
          <image v-if="tempCoverUrl" :src="tempCoverUrl" mode="aspectFit" class="preview-image" />
          <button @click="selectCover" class="upload-btn">{{ tempCoverUrl ? '更换封面' : '选择封面' }}</button>
        </view>
      </view>
      
      <view class="form-item">
        <text class="label">视频文件</text>
        <view class="upload-container">
          <view v-if="tempVideoInfo" class="video-info">
            <text class="video-name">{{ tempVideoInfo.name }}</text>
            <text class="video-size">{{ formatFileSize(tempVideoInfo.size) }}</text>
            <text class="video-duration" v-if="tempVideoInfo.duration">时长: {{ formatDuration(tempVideoInfo.duration) }}</text>
          </view>
          <button @click="selectVideo" class="upload-btn">{{ tempVideoInfo ? '更换视频' : '选择视频' }}</button>
        </view>
        <text class="hint">支持mp4、mov、avi格式，大小不超过200MB</text>
      </view>
      
      <view class="form-item" v-if="tempVideoInfo">
        <text class="label">视频预览</text>
        <video
          :src="tempVideoInfo.tempPath"
          controls
          class="video-preview"
          :poster="tempCoverUrl"
        />
      </view>
      
      <button 
        @click="submitContent" 
        type="primary" 
        class="submit-btn"
        :disabled="uploading"
      >
        {{ uploading ? '上传中...' : '发布视频' }}
      </button>
      
      <!-- 上传进度 -->
      <view v-if="uploading" class="upload-progress">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: uploadProgress + '%' }"></view>
        </view>
        <text class="progress-text">{{ uploadProgress }}%</text>
      </view>
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
  type: 2, // 2-视频
  coverUrl: '',
  mediaUrl: '',
  categoryId: null,
  tags: ''
})

const categories = ref([])
const categoryIndex = ref(0)
const tempCoverUrl = ref('') // 临时预览用的封面图URL
const tempCoverFile = ref(null) // 临时存储选择的封面文件
const tempVideoInfo = ref(null) // 临时存储选择的视频文件信息
const uploading = ref(false) // 上传状态
const uploadProgress = ref(0) // 上传进度

// 获取分类列表
const fetchCategories = async () => {
  try {
    console.log('开始获取分类列表...')
    const res = await proxy.$http.get('/api/category/all')
    console.log('分类列表API响应:', res)
    if(res.code === 200) {
      categories.value = res.data
      console.log('获取分类列表成功:', categories.value)
    } else {
      throw new Error(res.message || '获取分类列表失败')
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
  console.log('分类选择改变:', e.detail.value)
  categoryIndex.value = e.detail.value
  formData.categoryId = categories.value[categoryIndex.value].id
  console.log('选择的分类ID:', formData.categoryId)
}

// 选择封面图
const selectCover = async () => {
  try {
    console.log('开始选择封面图...')
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
    
    console.log('选择封面图成功:', tempFilePaths[0])
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

// 选择视频文件
const selectVideo = async () => {
  try {
    console.log('开始选择视频文件...')
    
    // 使用 uni.chooseVideo 选择视频
    const videoResult = await new Promise((resolve, reject) => {
      uni.chooseVideo({
        sourceType: ['album', 'camera'],
        maxDuration: 300, // 最大5分钟
        success: resolve,
        fail: reject
      })
    })
    
    console.log('选择视频成功:', videoResult)
    
    // 检查文件大小（200MB限制）
    const maxSize = 200 * 1024 * 1024 // 200MB
    if (videoResult.size > maxSize) {
      throw new Error('视频文件大小不能超过200MB')
    }
    
    // 保存视频信息
    tempVideoInfo.value = {
      tempPath: videoResult.tempFilePath,
      name: `video_${Date.now()}.mp4`,
      size: videoResult.size,
      duration: videoResult.duration
    }
    
    console.log('视频文件信息:', tempVideoInfo.value)
    uni.showToast({
      title: '已选择视频文件',
      icon: 'success'
    })
  } catch(e) {
    console.error('选择视频失败:', e)
    uni.showToast({
      title: e.message || '选择视频失败',
      icon: 'none'
    })
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化时长
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 上传封面图
const uploadCover = async (contentId) => {
  try {
    console.log('开始上传封面图，内容ID:', contentId)
    if (!tempCoverFile.value) {
      throw new Error('请先选择封面图')
    }

    // 上传图片 - 使用video类型，路径为video/resource/{contentId}，标记为封面
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
          path: `video/resource/${contentId}/video_cover`,
          fileName: 'video_cover.jpg'
        },
        success: resolve,
        fail: reject
      })
    })
    
    console.log('封面图上传响应:', uploadRes)
    const res = JSON.parse(uploadRes.data)
    console.log('解析后的封面图响应:', res)
    
    if(res.code === 200) {
      const coverUrl = res.data.originUrl
      console.log('获取到的封面图URL:', coverUrl)
      return coverUrl
    } else {
      throw new Error(res.message || '封面图上传失败')
    }
  } catch(e) {
    console.error('封面图上传失败:', e)
    throw e
  }
}

// 上传视频文件
const uploadVideo = async (contentId) => {
  try {
    console.log('开始上传视频文件，内容ID:', contentId)
    if (!tempVideoInfo.value) {
      throw new Error('请先选择视频文件')
    }

    // 上传视频 - 路径为video/resource/{contentId}，文件名固定为video.mp4
    const uploadRes = await new Promise((resolve, reject) => {
      const uploadTask = uni.uploadFile({
        url: proxy.$http.baseUrl + '/api/file/upload',
        filePath: tempVideoInfo.value.tempPath,
        name: 'file',
        header: {
          'accessToken': uni.getStorageSync('token')
        },
        formData: {
          type: 'video',
          path: `video/resource/${contentId}`,
          fileName: 'video.mp4'
        },
        success: resolve,
        fail: reject
      })
      
      // 监听上传进度
      uploadTask.onProgressUpdate((res) => {
        uploadProgress.value = res.progress
        console.log('视频上传进度:', res.progress + '%')
      })
    })
    
    console.log('视频上传响应:', uploadRes)
    const res = JSON.parse(uploadRes.data)
    console.log('解析后的视频响应:', res)
    
    if(res.code === 200) {
      const videoUrl = res.data.originUrl
      console.log('获取到的视频URL:', videoUrl)
      return videoUrl
    } else {
      throw new Error(res.message || '视频上传失败')
    }
  } catch(e) {
    console.error('视频上传失败:', e)
    throw e
  }
}

// 提交视频内容
const submitContent = async () => {
  // 验证必填字段
  if(!formData.title) {
    uni.showToast({
      title: '请输入视频标题',
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
  
  if(!tempVideoInfo.value) {
    uni.showToast({
      title: '请选择视频文件',
      icon: 'none'
    })
    return
  }
  
  if(!formData.content) {
    uni.showToast({
      title: '请输入视频简介',
      icon: 'none'
    })
    return
  }
  
  try {
    uploading.value = true
    uploadProgress.value = 0
    console.log('开始发布视频内容...')
    
    // 1. 创建视频内容记录（先用临时数据）
    console.log('第一步：创建视频内容记录')
    const createRes = await proxy.$http.post('/api/content', {
      ...formData,
      coverUrl: '', // 先不设置封面图URL
      mediaUrl: '' // 先不设置视频URL
    })
    
    if(createRes.code !== 200) {
      throw new Error(createRes.message || '创建视频内容失败')
    }
    
    const contentId = createRes.data.id
    console.log('视频内容创建成功，ID:', contentId)
    
    // 2. 上传封面图
    console.log('第二步：上传封面图')
    uploadProgress.value = 25
    const coverUrl = await uploadCover(contentId)
    
    // 3. 上传视频文件
    console.log('第三步：上传视频文件')
    uploadProgress.value = 50
    const videoUrl = await uploadVideo(contentId)
    
    // 4. 更新内容记录（使用真实的URL）
    console.log('第四步：更新内容记录')
    uploadProgress.value = 90
    
    const updateData = {
      ...formData,
      coverUrl,
      mediaUrl: videoUrl
    }
    console.log('准备更新视频内容，数据:', updateData)
    
    const updateRes = await proxy.$http.put(`/api/content/${contentId}`, updateData)
    
    if(updateRes.code === 200) {
      uploadProgress.value = 100
      console.log('视频发布成功')
      uni.showToast({
        title: '视频发布成功',
        icon: 'success'
      })
      
      // 重置表单
      setTimeout(() => {
        resetForm()
      }, 1500)
    } else {
      throw new Error(updateRes.message || '更新视频内容失败')
    }
  } catch(e) {
    console.error('视频发布失败:', e)
    uni.showToast({
      title: e.message || '视频发布失败',
      icon: 'none'
    })
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    title: '',
    content: '',
    type: 2,
    coverUrl: '',
    mediaUrl: '',
    categoryId: null,
    tags: ''
  })
  tempCoverFile.value = null
  tempCoverUrl.value = ''
  tempVideoInfo.value = null
  categoryIndex.value = 0
  
  console.log('视频表单已重置')
}

// 页面加载时获取分类列表
onMounted(() => {
  console.log('视频管理页面已挂载，开始获取分类列表')
  fetchCategories()
})
</script>

<style scoped>
.video-manage {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.form-container {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-item {
  margin-bottom: 30px;
}

.label {
  display: block;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

input {
  width: 100%;
  height: 44px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 15px;
  font-size: 14px;
  box-sizing: border-box;
}

.picker {
  width: 100%;
  height: 44px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 15px;
  font-size: 14px;
  line-height: 44px;
  background-color: #fff;
  color: #333;
}

.textarea-content {
  width: 100%;
  min-height: 100px;
  max-height: 200px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  font-size: 14px;
  line-height: 1.5;
  box-sizing: border-box;
  resize: vertical;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.upload-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.preview-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.video-info {
  flex: 1;
  min-width: 200px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #ddd;
}

.video-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.video-size,
.video-duration {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 3px;
}

.upload-btn {
  height: 44px;
  line-height: 44px;
  font-size: 14px;
  padding: 0 20px;
  border-radius: 8px;
  background-color: #007AFF;
  color: white;
  border: none;
}

.upload-btn:active {
  background-color: #0056CC;
}

.hint {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.video-preview {
  width: 100%;
  max-width: 400px;
  height: 225px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.submit-btn {
  width: 100%;
  height: 50px;
  line-height: 50px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background-color: #007AFF;
  color: white;
  border: none;
  margin-top: 20px;
}

.submit-btn:disabled {
  background-color: #ccc;
  color: #999;
}

.submit-btn:active:not(:disabled) {
  background-color: #0056CC;
}

.upload-progress {
  margin-top: 20px;
  text-align: center;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background-color: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background-color: #007AFF;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .video-manage {
    padding: 15px;
  }
  
  .form-container {
    padding: 20px;
  }
  
  .upload-container {
    flex-direction: column;
    align-items: stretch;
  }
  
  .preview-image {
    width: 100px;
    height: 100px;
  }
  
  .video-preview {
    height: auto;
    aspect-ratio: 16/9;
  }
}
</style> 