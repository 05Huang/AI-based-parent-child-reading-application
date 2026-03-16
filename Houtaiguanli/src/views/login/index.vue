<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 简洁头部 -->
      <div class="login-header">
        <img class="logo" src="@/assets/logo.svg" alt="logo" />
        <h1 class="title">阅桥亲子阅读APP管理</h1>
        <p class="subtitle">登录您的账户</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 记住我 -->
        <div class="login-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        </div>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            class="login-button"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 错误提示 -->
      <div v-if="errorMessage" class="error-message">
        <el-alert
          :title="errorMessage"
          type="error"
          :closable="true"
          @close="errorMessage = ''"
        />
      </div>

      <!-- 注册链接 -->
      <div class="register-link">
        <span>还没有账号？</span>
        <el-link type="primary" @click="goToRegister">立即注册</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 登录状态
const loading = ref(false)
const errorMessage = ref('')
const rememberMe = ref(false)
const loginFormRef = ref(null)

const loginForm = reactive({
  username: localStorage.getItem('remember_username') || '',
  password: ''
})

// 登录表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'change' },
    { min: 2, max: 64, message: '用户名长度在 2 到 64 个字符', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'change' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'change' }
  ]
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  if (!loginFormRef.value) return

  // 清除之前的错误状态
  errorMessage.value = ''
  
  // 基本输入检查（不使用表单验证）
  if (!loginForm.username || !loginForm.username.trim()) {
    ElMessage.error('请输入用户名')
    return
  }
  
  if (!loginForm.password || !loginForm.password.trim()) {
    ElMessage.error('请输入密码')
    return
  }

  loading.value = true

  // 确保密码是字符串类型
  const credentials = {
    username: String(loginForm.username).trim(),
    password: String(loginForm.password)
  }
  
  console.log('准备登录，凭证类型:', typeof credentials.username, typeof credentials.password)
  console.log('凭证内容:', credentials)

  try {
    await authStore.doLogin(credentials)

    // 保存用户名（如果勾选了记住我）
    if (rememberMe.value) {
      localStorage.setItem('remember_username', loginForm.username.trim())
    } else {
      localStorage.removeItem('remember_username')
    }

    ElMessage.success('登录成功，正在跳转...')
    
    // 延迟跳转以显示成功信息
    setTimeout(() => {
      router.push('/dashboard-layout/dashboard')
    }, 500)
  } catch (apiError) {
    loading.value = false
    
    // 处理API错误
    const errorMsg = apiError.response?.data?.message || 
                     apiError.message || 
                     '登录失败，请检查用户名和密码'
    
    errorMessage.value = errorMsg
    console.error('登录API错误:', apiError)
    
    ElMessage.error(errorMsg)
  }
}

/**
 * 跳转到注册页面
 */
const goToRegister = () => {
  console.log('点击了注册链接，准备跳转到注册页面')
  try {
    router.push('/register')
    console.log('路由跳转命令已执行')
  } catch (error) {
    console.error('跳转失败:', error)
  }
}

/**
 * 生命周期：页面加载时检查是否已登录
 */
onMounted(() => {
  // 如果已登录，重定向到首页
  if (authStore.isLoggedIn) {
    router.push('/dashboard-layout/dashboard')
  }
  
  // 如果URL中有username参数（从注册页面跳转过来），自动填入
  if (route.query.username) {
    loginForm.username = route.query.username
    ElMessage.success('注册成功！请输入密码登录')
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  background: url('@/assets/beijing.png') no-repeat center center;
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
    background: radial-gradient(1200px 600px at 20% 10%, rgba(255, 206, 138, 0.45) 0%, rgba(255, 206, 138, 0) 60%),
      radial-gradient(900px 500px at 80% 20%, rgba(255, 138, 168, 0.35) 0%, rgba(255, 138, 168, 0) 65%),
      linear-gradient(180deg, rgba(15, 23, 42, 0.25) 0%, rgba(15, 23, 42, 0.45) 100%);
    backdrop-filter: blur(2px);
  }

  .login-box {
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 450px;
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

    .login-header {
      margin-bottom: 22px;

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
        margin: 0;
      }
    }

    .login-form {
      margin-top: 6px;

      .el-form-item {
        margin-bottom: 18px;

        &:last-of-type {
          margin-bottom: 16px;
        }
      }

      .el-input {
        height: 44px;

        :deep(.el-input__wrapper) {
          background-color: rgba(248, 250, 252, 0.85);
          border: 1px solid rgba(148, 163, 184, 0.35);
          border-radius: 12px;
          box-shadow: none;
          transition: all 0.3s;

          &:hover {
            background-color: rgba(255, 255, 255, 0.92);
            border-color: rgba(255, 122, 89, 0.55);
          }
        }

        :deep(.el-input__wrapper.is-focus) {
          background-color: rgba(255, 255, 255, 0.98);
          border-color: rgba(255, 122, 89, 0.75);
          box-shadow: 0 0 0 4px rgba(255, 122, 89, 0.18);
        }

        :deep(.el-input__prefix-inner) {
          color: rgba(15, 23, 42, 0.55);
        }

        :deep(.el-input__inner) {
          font-size: 14px;

          &::placeholder {
            color: rgba(15, 23, 42, 0.38);
          }
        }
      }

      .login-button {
        width: 100%;
        height: 44px;
        font-size: 16px;
        font-weight: 700;
        letter-spacing: 0.6px;
        border-radius: 12px;
        background: linear-gradient(90deg, #ff7a59 0%, #ffb86c 100%);
        border: none;
        margin-top: 10px;
        box-shadow: 0 14px 30px rgba(255, 122, 89, 0.35);

        &:hover {
          background: linear-gradient(90deg, #ff7a59 0%, #ffd36a 100%);
          transform: translateY(-2px);
          box-shadow: 0 18px 36px rgba(255, 122, 89, 0.42);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }

    .login-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 2px 0 10px;
      font-size: 14px;

      .el-checkbox {
        :deep(.el-checkbox__label) {
          color: rgba(15, 23, 42, 0.72);
        }
      }

      .el-link {
        font-size: 12px;
      }
    }

    .error-message {
      margin-bottom: 20px;
      animation: slideIn 0.3s ease-in;

      @keyframes slideIn {
        from {
          opacity: 0;
          transform: translateY(-10px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }
    }

    .register-link {
      margin-top: 20px;
      text-align: center;
      padding: 10px;
      background-color: rgba(255, 255, 255, 0.55);
      border-radius: 12px;
      border: 1px solid rgba(148, 163, 184, 0.22);
      transition: all 0.3s;

      &:hover {
        background-color: rgba(255, 255, 255, 0.72);
      }

      .el-link {
        font-size: 13px;
        font-weight: 500;

        &:hover {
          transform: translateY(-1px);
        }
      }
    }
  }
}
</style> 
