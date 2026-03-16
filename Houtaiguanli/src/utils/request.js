import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8888', // 从环境变量获取 API 地址，默认为本地 Spring Boot
  timeout: 15000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      // 后端期望的header名称是 'accessToken' 而不是 'Authorization'
      config.headers['accessToken'] = token
    }
    
    // 如果数据是对象且不是 FormData，确保设置 Content-Type 为 JSON
    if (config.data && !(config.data instanceof FormData)) {
      if (!config.headers['Content-Type'] || config.headers['Content-Type'].includes('urlencoded')) {
        config.headers['Content-Type'] = 'application/json;charset=UTF-8'
      }
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    console.log('响应拦截器 - 原始响应:', res)
    
    // 这里根据后端的实际响应结构进行调整
    // Spring Boot 后端使用 Result 类，code 为 200 表示成功
    if (res.code === 200 || res.code === '200') {
      return res.data || res // 返回数据部分
    }
    
    // 处理 token 过期或无效 - 自动跳转到登录页
    if (res.code === 401 || res.message?.includes('token') || res.message?.includes('无效') || res.message?.includes('过期')) {
      console.warn('Token 已过期或无效，即将跳转到登录页')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 延迟跳转，给用户一点时间看到提示
      setTimeout(() => {
        window.location.href = '/login'
      }, 1000)
      return Promise.reject(new Error(res.message || 'Token 无效或已过期'))
    }
    
    // 处理其他状态码 - 不显示 ElMessage，让业务代码自己处理
    const errorMsg = res.message || res.msg || res.error || '请求失败'
    console.error('后端返回错误:', errorMsg, res)
    
    return Promise.reject(new Error(errorMsg))
  },
  error => {
    console.error('响应错误:', error)
    console.error('错误响应数据:', error.response?.data)
    
    // 提取更详细的错误信息
    let errorMsg = '系统繁忙，请稍后重试'
    let statusCode = error.response?.status
    
    if (error.response?.data) {
      const errorData = error.response.data
      errorMsg = errorData.message || errorData.msg || errorData.error || errorMsg
    } else if (error.message) {
      errorMsg = error.message
    }
    
    // 处理 401 Unauthorized - Token 过期或无效
    if (statusCode === 401 || errorMsg?.includes('token') || errorMsg?.includes('无效') || errorMsg?.includes('过期')) {
      console.warn('Token 已过期或无效，即将跳转到登录页')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      setTimeout(() => {
        window.location.href = '/login'
      }, 1000)
      return Promise.reject(new Error('Token 无效或已过期，请重新登录'))
    }
    
    // 网络错误
    if (error.code === 'ECONNABORTED') {
      errorMsg = '请求超时，请稍后重试'
    } else if (error.code === 'ERR_NETWORK') {
      errorMsg = '网络错误，请检查网络连接'
    }
    
    console.error('最终错误消息:', errorMsg)
    
    // 创建错误对象，包含完整的响应信息
    const finalError = new Error(errorMsg)
    finalError.response = error.response
    finalError.code = error.code
    
    return Promise.reject(finalError)
  }
)

export default service 