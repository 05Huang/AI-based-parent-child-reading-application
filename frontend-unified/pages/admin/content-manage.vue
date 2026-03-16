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
        <view class="editor-container">
          <Editor
            api-key="t05fj6mru5kigg6jgoo92klypwwi1f79f3p5wik2in42dw80"
            :init="{
              height: 500,
              toolbar_mode: 'sliding',
              plugins: [
                // 核心编辑功能
                'anchor', 'autolink', 'charmap', 'codesample', 'emoticons', 'link', 'lists', 'media', 
                'searchreplace', 'table', 'visualblocks', 'wordcount',
                // 高级功能
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
              // 禁用自动上传，图片插入时仅保存临时数据
              images_upload_handler: (blobInfo, success, failure, progress) => {
                console.log('图片插入处理开始，保存临时数据等待发布时上传')
                
                try {
                  const file = blobInfo.blob()
                  const fileName = blobInfo.filename() || `image_${Date.now()}.jpg`
                  
                  // 创建临时URL用于在编辑器中显示图片
                  const tempUrl = URL.createObjectURL(file)
                  
                  // 初始化临时图片数组
                  if (!formData.tempImages) {
                    formData.tempImages = []
                  }
                  
                  // 保存临时图片信息，发布时再上传
                  const tempImageInfo = {
                    blobInfo: blobInfo,
                    file: file,
                    tempUrl: tempUrl,
                    fileName: fileName,
                    id: `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
                  }
                  
                  formData.tempImages.push(tempImageInfo)
                  console.log('临时图片已保存:', { fileName, tempUrl, id: tempImageInfo.id })
                  
                  // 返回临时URL给编辑器，让用户可以看到图片
                  success(tempUrl)
                } catch (error) {
                  console.error('处理图片失败:', error)
                  if (failure && typeof failure === 'function') {
                    failure('处理图片失败: ' + (error.message || error))
                  }
                }
              },
              // 禁用拼写检查
              browser_spellcheck: false,
              // 其他配置
              language: 'zh_CN',
              language_url: 'https://cdn.tiny.cloud/1/t05fj6mru5kigg6jgoo92klypwwi1f79f3p5wik2in42dw80/tinymce/6/langs/zh_CN.js',
              // 自动保存配置
              autosave_interval: '30s',
              autosave_prefix: 'tinymce-autosave-{path}-{id}-',
              // 其他配置
              branding: false, // 移除 TinyMCE 品牌
              menubar: true, // 显示菜单栏
              statusbar: true, // 显示状态栏
              resize: true, // 允许调整大小
              paste_data_images: true, // 允许粘贴图片
              image_advtab: true, // 图片高级选项卡
              image_title: true, // 图片标题
              automatic_uploads: false, // 禁用自动上传，发布时再上传
              file_picker_types: 'image', // 只允许选择图片
              tinycomments_mode: 'embedded',
              tinycomments_author: 'Author name',
              mergetags_list: [
                { value: 'First.Name', title: 'First Name' },
                { value: 'Email', title: 'Email' },
              ]
            }"
            v-model="formData.content"
          />
        </view>
      </view>
      
      <button @click="submitContent" type="primary" class="submit-btn">发布文章</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, onMounted } from 'vue'
import Editor from '@tinymce/tinymce-vue'

const { proxy } = getCurrentInstance()

// TinyMCE 配置
const tinymceInit = {
  // 替换为你的 API Key
  apiKey: 'your-api-key-here',
  
  // 语言设置
  language: 'zh_CN',
  language_url: '/tinymce/langs/zh_CN.js',
  
  // 高度设置
  height: 500,
  
  // 工具栏配置
  toolbar: [
    'undo redo | formatselect | bold italic underline strikethrough | alignleft aligncenter alignright alignjustify',
    'bullist numlist | outdent indent | link image media | removeformat | code | fullscreen',
    'fontsizeselect | forecolor backcolor | table | hr | subscript superscript | charmap emoticons'
  ],
  
  // 插件配置
  plugins: [
    'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
    'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
    'insertdatetime', 'media', 'table', 'help', 'wordcount', 'emoticons'
  ],
  
  // 自定义图片上传
  images_upload_handler: async (blobInfo, progress) => {
    try {
      const file = blobInfo.blob()
      
      // 获取文章ID，如果还没有创建文章，先创建
      let articleId = formData.id
      if (!articleId) {
        const createRes = await proxy.$http.post('/api/content', {
          ...formData,
          content: '', // 先不设置内容
          coverUrl: '', // 先不设置封面图URL
          imageUrls: [] // 先不设置图片URL
        })
        
        if (createRes.code !== 200) {
          throw new Error(createRes.message || '创建文章失败')
        }
        
        articleId = createRes.data.id
        formData.id = articleId
      }
      
      // 计算图片索引
      const imageIndex = formData.imageUrls.length + 1
      
      // 上传图片
      const uploadRes = await new Promise((resolve, reject) => {
        uni.uploadFile({
          url: proxy.$http.baseUrl + '/api/file/upload',
          file,
          name: 'file',
          header: {
            'accessToken': uni.getStorageSync('token')
          },
          formData: {
            type: 'image',
            path: `article/${articleId}`,
            fileName: `article_image_${imageIndex}.jpg`
          },
          success: resolve,
          fail: reject
        })
      })
      
      const res = JSON.parse(uploadRes.data)
      if (res.code === 200) {
        const imageUrl = res.data.originUrl
        formData.imageUrls.push(imageUrl)
        return imageUrl
      } else {
        throw new Error(res.message || '上传失败')
      }
    } catch (e) {
      console.error('文章图片上传失败:', e)
      throw e
    }
  },
  
  // 设置为中文
  language: 'zh_CN',
  
  // 自动保存配置
  autosave_interval: '30s',
  autosave_prefix: 'tinymce-autosave-{path}-{id}-',
  
  // 其他配置
  branding: false, // 移除 TinyMCE 品牌
  menubar: true, // 显示菜单栏
  statusbar: true, // 显示状态栏
  resize: true, // 允许调整大小
  
  // 内容样式
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
  `
}

// 响应式数据
const formData = reactive({
  id: null, // 添加id字段
  title: '',
  content: '',
  type: 1, // 1-图文
  coverUrl: '',
  categoryId: null,
  tags: '',
  imageUrls: [], // 存储文章中的图片URL
  tempImages: [] // 存储临时图片信息，发布时上传
})

const categories = ref([])
const categoryIndex = ref(0)
const tempCoverUrl = ref('') // 临时预览用的封面图URL
const tempCoverFile = ref(null) // 临时存储选择的文件

// 切换预览
const togglePreview = () => {
  showPreview.value = !showPreview.value
}

// 执行编辑器命令
const execCommand = (name, value = null) => {
  if (!editorCtx.value) return
  if (value) {
    editorCtx.value.format(name, value)
  } else {
    editorCtx.value.format(name)
  }
}

// 插入链接
const insertLink = async () => {
  try {
    const url = await uni.showModal({
      title: '插入链接',
      content: '请输入链接地址',
      editable: true,
      placeholderText: 'https://'
    })
    
    if (url.confirm && url.content) {
      editorCtx.value.insertLink({
        href: url.content,
        text: url.content
      })
    }
  } catch(e) {
    console.error('插入链接失败:', e)
  }
}

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

// 上传文章内容图片
const uploadContentImage = async (tempFilePath, articleId, imageIndex) => {
  try {
    const uploadRes = await new Promise((resolve, reject) => {
      uni.uploadFile({
        url: proxy.$http.baseUrl + '/api/file/upload',
        filePath: tempFilePath,
        name: 'file',
        header: {
          'accessToken': uni.getStorageSync('token')
        },
        formData: {
          type: 'image',
          path: `article/${articleId}`,
          fileName: `article_image_${imageIndex}.jpg`
        },
        success: resolve,
        fail: reject
      })
    })
    
    const res = JSON.parse(uploadRes.data)
    if(res.code === 200) {
      return res.data.originUrl
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch(e) {
    console.error('文章图片上传失败:', e)
    throw e
  }
}

// 上传blob图片（从TinyMCE编辑器中的图片）
const uploadBlobImage = async (file, articleId, fileName) => {
  try {
    console.log('开始上传blob图片:', { fileName, articleId })
    
    // 在H5环境下，需要将blob转换为File对象
    let uploadFile = file
    if (file instanceof Blob && !(file instanceof File)) {
      // 将Blob转换为File对象
      uploadFile = new File([file], fileName, { type: file.type })
    }
    
    // 创建FormData（用于H5环境）
    if (typeof FormData !== 'undefined') {
      const formData = new FormData()
      formData.append('file', uploadFile)
      formData.append('type', 'image')
      formData.append('path', `article/${articleId}`)
      formData.append('fileName', fileName)
      
      // 使用fetch上传（H5环境）
      const response = await fetch(proxy.$http.baseUrl + '/api/file/upload', {
        method: 'POST',
        headers: {
          'accessToken': uni.getStorageSync('token')
        },
        body: formData
      })
      
      const result = await response.json()
      
      if (result.code === 200) {
        const imageUrl = result.data.originUrl
        console.log('blob图片上传成功:', imageUrl)
        console.log('URL长度:', imageUrl ? imageUrl.length : 'undefined')
        console.log('URL详细信息:', {
          url: imageUrl,
          length: imageUrl ? imageUrl.length : 0,
          type: typeof imageUrl
        })
        return imageUrl
      } else {
        throw new Error(result.message || '上传失败')
      }
    } else {
      // APP环境下，将blob转换为临时文件路径
      const tempFilePath = await new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => {
          // 创建临时文件
          const base64 = reader.result.split(',')[1]
          const tempPath = `${wx.env.USER_DATA_PATH}/${fileName}`
          
          wx.getFileSystemManager().writeFile({
            filePath: tempPath,
            data: base64,
            encoding: 'base64',
            success: () => resolve(tempPath),
            fail: reject
          })
        }
        reader.onerror = reject
        reader.readAsDataURL(file)
      })
      
      // 使用uni.uploadFile上传
      const uploadRes = await new Promise((resolve, reject) => {
        uni.uploadFile({
          url: proxy.$http.baseUrl + '/api/file/upload',
          filePath: tempFilePath,
          name: 'file',
          header: {
            'accessToken': uni.getStorageSync('token')
          },
          formData: {
            type: 'image',
            path: `article/${articleId}`,
            fileName: fileName
          },
          success: resolve,
          fail: reject
        })
      })
      
      const result = JSON.parse(uploadRes.data)
      
      if (result.code === 200) {
        const imageUrl = result.data.originUrl
        console.log('blob图片上传成功:', imageUrl)
        console.log('URL长度:', imageUrl ? imageUrl.length : 'undefined')
        console.log('URL详细信息:', {
          url: imageUrl,
          length: imageUrl ? imageUrl.length : 0,
          type: typeof imageUrl
        })
        return imageUrl
      } else {
        throw new Error(result.message || '上传失败')
      }
    }
  } catch(e) {
    console.error('blob图片上传失败:', e)
    throw e
  }
}

// 从内容中提取所有图片URL（备用方案）
const extractImagesFromContent = (content) => {
  const images = []
  if (!content) return images
  
  // 使用正则表达式提取图片URL
  const pattern = /<img[^>]+src=["']([^"']+)["'][^>]*>/g
  let match
  
  while ((match = pattern.exec(content)) !== null) {
    const imageUrl = match[1]
    console.log('从内容中提取到图片URL:', {
      url: imageUrl.substring(0, 100) + (imageUrl.length > 100 ? '...' : ''), // 截取前100个字符显示
      length: imageUrl.length,
      isBlob: imageUrl.startsWith('blob:'),
      isHttp: imageUrl.startsWith('http'),
      isDataUrl: imageUrl.startsWith('data:')
    })
    images.push(imageUrl)
  }
  
  console.log('extractImagesFromContent结果:', images.length, '张图片')
  return images
}

// 将base64 data URL转换为Blob对象
const dataURLToBlob = (dataURL) => {
  const parts = dataURL.split(',')
  const mimeMatch = parts[0].match(/:(.*?);/)
  const mime = mimeMatch ? mimeMatch[1] : 'image/jpeg'
  const byteString = atob(parts[1])
  const arrayBuffer = new ArrayBuffer(byteString.length)
  const uint8Array = new Uint8Array(arrayBuffer)
  
  for (let i = 0; i < byteString.length; i++) {
    uint8Array[i] = byteString.charCodeAt(i)
  }
  
  return new Blob([arrayBuffer], { type: mime })
}

// 处理内容中的base64图片，上传到MinIO并替换URL
const processBase64ImagesInContent = async (content, articleId) => {
  if (!content || !articleId) return content
  
  console.log('开始处理内容中的base64图片...')
  
  // 查找所有base64图片
  const base64Pattern = /<img[^>]+src=["'](data:image\/[^"']+)["'][^>]*>/g
  const matches = []
  let match
  
  while ((match = base64Pattern.exec(content)) !== null) {
    matches.push({
      fullMatch: match[0],
      dataUrl: match[1]
    })
  }
  
  if (matches.length === 0) {
    console.log('没有找到base64图片')
    return content
  }
  
  console.log(`找到 ${matches.length} 张base64图片，开始上传...`)
  let updatedContent = content
  
  // 确保imageUrls数组存在
  if (!formData.imageUrls) {
    formData.imageUrls = []
  }
  
  for (let i = 0; i < matches.length; i++) {
    const { dataUrl } = matches[i]
    try {
      console.log(`处理第 ${i + 1} 张base64图片...`)
      
      // 将base64转换为Blob
      const blob = dataURLToBlob(dataUrl)
      const fileName = `article_image_${i + 1}.jpg`
      
      // 上传到MinIO
      const realUrl = await uploadBlobImage(blob, articleId, fileName)
      
      // 替换内容中的base64 URL为真实URL
      updatedContent = updatedContent.replace(dataUrl, realUrl)
      
      // 添加到imageUrls数组
      formData.imageUrls.push(realUrl)
      
      console.log(`第 ${i + 1} 张base64图片处理完成:`, realUrl)
    } catch (error) {
      console.error(`第 ${i + 1} 张base64图片处理失败:`, error)
      throw new Error(`base64图片处理失败: ${error.message}`)
    }
  }
  
  console.log('所有base64图片处理完成')
  return updatedContent
}

// 处理所有临时图片上传并替换编辑器中的URL
const uploadTempImagesAndReplaceUrls = async (articleId) => {
  console.log('开始处理临时图片上传...')
  
  // 清空之前的图片URL数组，重新收集
  formData.imageUrls = []
  
  if (!formData.tempImages || formData.tempImages.length === 0) {
    console.log('没有临时图片需要上传，检查是否有base64图片需要处理')
    
    // 处理内容中的base64图片
    const updatedContent = await processBase64ImagesInContent(formData.content, articleId)
    
    // 如果没有base64图片，从内容中提取已有的HTTP URL
    if (updatedContent === formData.content) {
      const extractedUrls = extractImagesFromContent(formData.content)
      const httpUrls = extractedUrls.filter(url => url.startsWith('http'))
      formData.imageUrls = httpUrls
      console.log('从内容中提取到的HTTP图片URL:', httpUrls)
    }
    
    return updatedContent
  }
  
  console.log(`开始上传 ${formData.tempImages.length} 张临时图片`)
  let updatedContent = formData.content
  
  for (let i = 0; i < formData.tempImages.length; i++) {
    const tempImage = formData.tempImages[i]
    try {
      console.log(`上传第 ${i + 1} 张图片:`, tempImage.fileName)
      
      // 上传图片
      const realUrl = await uploadBlobImage(
        tempImage.file, 
        articleId, 
        `article_image_${i + 1}.jpg`
      )
      
      // 替换编辑器内容中的临时URL为真实URL
      console.log('准备替换URL:', {
        tempUrl: tempImage.tempUrl,
        realUrl: realUrl,
        tempUrlLength: tempImage.tempUrl ? tempImage.tempUrl.length : 0,
        realUrlLength: realUrl ? realUrl.length : 0
      })
      updatedContent = updatedContent.replace(tempImage.tempUrl, realUrl)
      console.log('URL替换完成')
      
      // 保存到imageUrls数组，这个数组会传递给后端
      formData.imageUrls.push(realUrl)
      
      console.log(`第 ${i + 1} 张图片上传完成，URL已替换:`, realUrl)
      console.log(`URL长度: ${realUrl ? realUrl.length : 'undefined'} 字符`)
    } catch (error) {
      console.error(`第 ${i + 1} 张图片上传失败:`, error)
      throw new Error(`图片 ${tempImage.fileName} 上传失败: ${error.message}`)
    }
  }
  
  // 处理内容中可能存在的base64图片
  console.log('检查是否还有base64图片需要处理...')
  updatedContent = await processBase64ImagesInContent(updatedContent, articleId)
  
  // 最终收集所有真实的HTTP图片URL
  const allImageUrls = extractImagesFromContent(updatedContent)
  const httpUrls = allImageUrls.filter(url => url.startsWith('http'))
  formData.imageUrls = httpUrls
  
  console.log('所有图片处理完成，最终imageUrls:', formData.imageUrls)
  return updatedContent
}

// 插入图片到编辑器
const insertImage = async () => {
  try {
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

    // 先使用本地路径插入图片
    const tempUrl = tempFilePaths[0]
    editorCtx.value.insertImage({
      src: tempUrl,
      alt: '文章图片',
      success: () => {
        console.log('图片插入成功')
      }
    })
    
    // 保存临时文件路径，等发布时再上传
    formData.imageUrls.push(tempUrl)
  } catch(e) {
    console.error('插入图片失败:', e)
    uni.showToast({
      title: e.message || '插入图片失败',
      icon: 'none'
    })
  }
}

// 修改提交内容方法
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
    console.log('开始发布文章...')
    
    // 如果已经有文章ID，直接更新
    if (formData.id) {
      console.log('更新现有文章，ID:', formData.id)
      
      // 先处理临时图片上传并替换URL
      const updatedContent = await uploadTempImagesAndReplaceUrls(formData.id)
      
      // 上传封面图
      const coverUrl = await uploadCover(formData.id)
      
      // 更新文章（使用更新后的内容和图片URL）
      const updateData = {
        ...formData,
        content: updatedContent,
        coverUrl,
        imageUrls: formData.imageUrls // 确保传递图片URL数组
      }
      console.log('准备更新文章，数据:', updateData)
      console.log('imageUrls数组详情:', {
        count: updateData.imageUrls ? updateData.imageUrls.length : 0,
        urls: updateData.imageUrls,
        urlLengths: updateData.imageUrls ? updateData.imageUrls.map(url => ({ url, length: url.length })) : []
      })
      
      const updateRes = await proxy.$http.put(`/api/content/${formData.id}`, updateData)
      
      if(updateRes.code === 200) {
        uni.showToast({
          title: '发布成功',
          icon: 'success'
        })
        resetForm()
      } else {
        throw new Error(updateRes.message || '更新文章失败')
      }
    } else {
      console.log('创建新文章')
      
      // 创建新文章（先用临时内容）
      const createRes = await proxy.$http.post('/api/content', {
        ...formData,
        coverUrl: '', // 先不设置封面图URL
        imageUrls: [] // 先不设置图片URL
      })
      
      if(createRes.code !== 200) {
        throw new Error(createRes.message || '创建文章失败')
      }
      
      const articleId = createRes.data.id
      formData.id = articleId
      console.log('文章创建成功，ID:', articleId)
      
      // 处理临时图片上传并替换URL
      const updatedContent = await uploadTempImagesAndReplaceUrls(articleId)
      
      // 上传封面图
      const coverUrl = await uploadCover(articleId)
      
      // 更新文章（使用真实的图片URL和内容）
      const updateData = {
        ...formData,
        content: updatedContent,
        coverUrl,
        imageUrls: formData.imageUrls // 确保传递图片URL数组
      }
      console.log('准备更新新创建的文章，数据:', updateData)
      
      const updateRes = await proxy.$http.put(`/api/content/${articleId}`, updateData)
      
      if(updateRes.code === 200) {
        uni.showToast({
          title: '发布成功',
          icon: 'success'
        })
        resetForm()
      } else {
        throw new Error(updateRes.message || '更新文章失败')
      }
    }
  } catch(e) {
    console.error('发布失败:', e)
    uni.showToast({
      title: e.message || '发布失败',
      icon: 'none'
    })
  }
}

// 重置表单
const resetForm = () => {
  // 清理临时图片的blob URL，避免内存泄露
  if (formData.tempImages && formData.tempImages.length > 0) {
    formData.tempImages.forEach(tempImage => {
      if (tempImage.tempUrl) {
        URL.revokeObjectURL(tempImage.tempUrl)
      }
    })
  }
  
  Object.assign(formData, {
    id: null,
    title: '',
    content: '',
    type: 1,
    coverUrl: '',
    categoryId: null,
    tags: '',
    imageUrls: [],
    tempImages: [] // 清空临时图片数据
  })
  tempCoverFile.value = null
  tempCoverUrl.value = ''
  
  console.log('表单已重置，临时数据已清理')
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

.editor-container {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  min-height: 500px;
}

/* TinyMCE 相关样式 */
:deep(.tox-tinymce) {
  border: none !important;
}

:deep(.tox-statusbar) {
  border-top: 1px solid #ddd !important;
}
</style> 