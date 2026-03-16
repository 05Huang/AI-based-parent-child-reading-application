<template>
  <div class="register-container">
    <div class="register-box">
      <!-- 简洁头部 -->
      <div class="register-header">
        <img class="logo" src="@/assets/logo.svg" alt="logo" />
        <h1 class="title">创建账户</h1>
        <p class="subtitle">开始使用阅桥亲子阅读APP管理系统</p>
      </div>

      <!-- 注册表单 -->
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
      >
        <!-- 头像上传 -->
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload-section">
            <div class="avatar-preview-box">
              <el-image v-if="registerForm.avatar" :src="registerForm.avatar" style="width: 80px; height: 80px" fit="cover" />
              <div v-else class="avatar-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </div>
            <el-upload
              class="avatar-uploader"
              :auto-upload="true"
              :http-request="handleAvatarUpload"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary" size="small">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名" clearable />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="昵称" clearable />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input v-model="registerForm.phone" placeholder="手机号" clearable />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码" show-password clearable />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" show-password clearable />
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button :loading="loading" type="primary" class="register-button" @click="handleRegister">
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>

        <!-- 返回登录 -->
        <el-form-item>
          <el-button class="back-button" @click="goToLogin">返回登录</el-button>
        </el-form-item>
      </el-form>

      <!-- 错误提示 -->
      <div v-if="errorMessage" class="error-message">
        <el-alert :title="errorMessage" type="error" :closable="true" @close="errorMessage = ''" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { registerAdmin } from '@/api/admin'
import { uploadUserAvatar } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 状态
const loading = ref(false)
const errorMessage = ref('')
const registerFormRef = ref(null)

// 注册表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  avatar: ''
})

// 表单验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 头像上传处理
const handleAvatarUpload = async (options) => {
  try {
    const response = await uploadUserAvatar(options.file)
    if (response && response.originUrl) {
      registerForm.avatar = response.originUrl
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error('获取头像URL失败')
    }
    return response
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
    throw error
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    // 验证表单
    await registerFormRef.value.validate()

    loading.value = true
    errorMessage.value = ''

    // 准备注册数据
    const registerData = {
      username: registerForm.username.trim(),
      password: registerForm.password,
      nickname: registerForm.nickname.trim(),
      phone: registerForm.phone.trim(),
      avatar: registerForm.avatar, // 包含头像URL
      userType: 2, // 2 = 管理员
      role: 0, // 0 = 管理员
      sex: 0, // 0 = 保密
      status: 1 // 1 = 正常状态
    }

    console.log('注册管理员数据:', registerData)

    // 调用管理员注册API
    const response = await registerAdmin(registerData)
    console.log('注册响应:', response)

    // 后端返回的是 LoginVO，包含 token，直接登录
    if (response.data && response.data.accessToken) {
      // 保存登录信息
      authStore.setToken(response.data.accessToken, response.data.refreshToken)
      
      // 获取用户信息
      try {
        await authStore.fetchUserInfo()
        console.log('用户信息已获取:', authStore.userInfo)
      } catch (fetchError) {
        console.warn('获取用户信息失败，但继续登录:', fetchError)
      }
      
      ElMessage.success({
        message: '管理员账号注册成功！正在跳转到首页...',
        duration: 2000
      })

      // 清空表单
      registerFormRef.value.resetFields()
      registerForm.avatar = ''

      // 延迟跳转到首页
      setTimeout(() => {
        router.push('/dashboard-layout/dashboard')
      }, 1500)
    } else {
      // 如果没有返回 token，跳转到登录页
      ElMessage.success({
        message: '管理员账号注册成功！正在跳转到登录页面...',
        duration: 2000
      })

      setTimeout(() => {
        router.push({
          path: '/login',
          query: { username: registerData.username }
        })
      }, 1500)
    }

    loading.value = false
  } catch (error) {
    loading.value = false

    console.error('注册失败:', error)
    console.error('错误详情:', error.response)

    // 提取错误信息
    let errorMsg = '注册失败，请检查输入信息'
    
    if (error.response?.data) {
      const errorData = error.response.data
      errorMsg = errorData.message || errorData.error || errorData.msg || errorMsg
    } else if (error.message) {
      errorMsg = error.message
    }

    errorMessage.value = errorMsg
    ElMessage.error(errorMsg)
  }
}

// 返回登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.register-container {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  background: url('@/assets/beijing2.png') no-repeat center center;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(1100px 560px at 18% 12%, rgba(255, 206, 138, 0.42) 0%, rgba(255, 206, 138, 0) 60%),
      radial-gradient(900px 520px at 82% 24%, rgba(255, 138, 168, 0.32) 0%, rgba(255, 138, 168, 0) 65%),
      linear-gradient(180deg, rgba(15, 23, 42, 0.22) 0%, rgba(15, 23, 42, 0.48) 100%);
    backdrop-filter: blur(2px);
  }
}

.register-box {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 500px;
  padding: 34px 36px 30px;
  background: rgba(255, 255, 255, 0.86);
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(12px);
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.28);
  text-align: center;

  @media (max-width: 768px) {
    padding: 28px 18px 24px;
  }
}

.register-header {
  margin-bottom: 22px;
}

.logo {
  width: 168px;
  height: auto;
  margin: 0 auto 14px;
  display: block;
  filter: drop-shadow(0 8px 14px rgba(15, 23, 42, 0.18));
}

.title {
  font-size: 30px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px;
  letter-spacing: 0.2px;
}

.subtitle {
  font-size: 13px;
  color: rgba(15, 23, 42, 0.7);
  letter-spacing: 0.8px;
  margin: 0 0 10px;
}

.register-form {
  width: 100%;
  margin-bottom: 20px;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__label) {
    color: rgba(15, 23, 42, 0.7);
    font-weight: 600;
  }

  :deep(.el-input__wrapper) {
    background-color: rgba(248, 250, 252, 0.85);
    border: 1px solid rgba(148, 163, 184, 0.35);
    border-radius: 12px;
    box-shadow: none;
    transition: all 0.3s;
  }

  :deep(.el-input__wrapper:hover) {
    background-color: rgba(255, 255, 255, 0.92);
    border-color: rgba(255, 122, 89, 0.55);
  }

  :deep(.el-input__wrapper.is-focus) {
    background-color: rgba(255, 255, 255, 0.98);
    border-color: rgba(255, 122, 89, 0.75);
    box-shadow: 0 0 0 4px rgba(255, 122, 89, 0.18);
  }

  :deep(.el-input__inner::placeholder) {
    color: rgba(15, 23, 42, 0.38);
  }
}

.register-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.6px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(90deg, #ff7a59 0%, #ffb86c 100%);
  box-shadow: 0 14px 30px rgba(255, 122, 89, 0.35);
  transition: all 0.2s ease;
}

.register-button:hover {
  transform: translateY(-2px);
  background: linear-gradient(90deg, #ff7a59 0%, #ffd36a 100%);
  box-shadow: 0 18px 36px rgba(255, 122, 89, 0.42);
}

.back-button {
  width: 100%;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.22);
  color: rgba(15, 23, 42, 0.78);
  transition: all 0.2s ease;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.72);
  transform: translateY(-1px);
}

.error-message {
  margin-bottom: 20px;
}

.avatar-upload-section {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
}

.avatar-preview-box {
  width: 80px;
  height: 80px;
  border: 1px dashed rgba(148, 163, 184, 0.6);
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.55);
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: rgba(15, 23, 42, 0.55);
}

.avatar-placeholder span {
  font-size: 12px;
  margin-top: 4px;
}

:deep(.el-icon) {
  font-size: 34px;
}

:deep(.avatar-uploader .el-upload) {
  cursor: pointer;
}
</style>
