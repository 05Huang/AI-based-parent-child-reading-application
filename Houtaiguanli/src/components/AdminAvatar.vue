<template>
  <div class="admin-avatar">
    <el-dropdown trigger="click" @command="handleCommand">
      <div class="avatar-wrapper">
        <el-avatar :size="40" :src="adminInfo.avatar" />
        <span class="admin-name">{{ adminInfo.nickname || adminInfo.username }}</span>
        <el-icon><CaretBottom /></el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="profile">
            <el-icon><User /></el-icon>个人信息
          </el-dropdown-item>
          <el-dropdown-item divided command="logout">
            <el-icon><SwitchButton /></el-icon>退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 个人信息编辑对话框 - 完全参照用户管理逻辑 -->
    <el-dialog v-model="profileVisible" title="个人信息编辑" width="500px" center destroy-on-close>
        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
        label-width="100px"
        >
        <!-- 用户名 - 显示但不可编辑 -->
          <el-form-item label="用户名" prop="username">
            <el-input v-model="profileForm.username" disabled />
          </el-form-item>

        <!-- 昵称 -->
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <!-- 头像 - 参照用户管理的头像上传逻辑 -->
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload">
            <el-image
              v-if="profileForm.avatar"
              :src="profileForm.avatar"
              style="width: 80px; height: 80px; border-radius: 4px"
            />
            <div v-else class="avatar-placeholder">点击上传头像</div>
            <el-upload
              class="avatar-uploader"
              action="#"
              :auto-upload="false"
              :on-change="handleAvatarSelect"
              accept="image/*"
            >
              <template #trigger>
                <el-button type="primary" size="small">选择图片</el-button>
              </template>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱（可选）" />
          </el-form-item>

        <!-- 手机号 - 显示但不可编辑 -->
          <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" disabled />
          </el-form-item>

        <!-- 角色 - 显示但不可编辑 -->
        <el-form-item label="角色" prop="role">
          <el-tag type="success">{{ getRoleText(profileForm.role) }}</el-tag>
          </el-form-item>

        <!-- 状态 - 显示但不可编辑 -->
        <el-form-item label="状态" prop="status">
          <el-tag :type="profileForm.status === 1 ? 'success' : 'danger'">
            {{ profileForm.status === 1 ? '正常' : '禁用' }}
          </el-tag>
          </el-form-item>

        <!-- 修改密码 -->
        <el-form-item label="密码">
          <el-button link type="primary" @click="showChangePassword = true">修改密码</el-button>
          </el-form-item>
        </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="profileVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveProfile" :loading="savingProfile">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showChangePassword" title="修改密码" width="400px" center destroy-on-close>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Setting, SwitchButton, CaretBottom } from '@element-plus/icons-vue'
import { getCurrentUser, getUserDetail, updateUser, uploadUserAvatar, changePassword } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息 - 用于显示
const adminInfo = reactive({
  id: null,
  username: '',
  nickname: '',
  avatar: '',
  email: '',
  phone: '',
  role: 0,
  userType: 2,
  status: 1
})

// 编辑表单 - 参照用户管理的逻辑
const profileFormRef = ref(null)
const profileForm = reactive({
  id: null,
  username: '',
  nickname: '',
  userType: 2,
  role: 0,
  phone: '',
  email: '',
  status: 1,
  avatar: '' // 只用于提交给后端的URL
})

// 表单验证规则
const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 对话框状态
const profileVisible = ref(false)
const showChangePassword = ref(false)

const savingProfile = ref(false)
const changingPassword = ref(false)

// 密码修改表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = { 0: '管理员', 1: '家长', 2: '孩子' }
  return roleMap[role] || '未知'
}

// 从后端加载当前用户信息 - 使用完整 API 获取
const loadUserInfo = async () => {
  try {
    console.log('正在加载用户信息...')
    
    // 第一步：获取当前用户 ID
    let userId = adminInfo.id
    if (!userId) {
      const currentUserRes = await getCurrentUser()
      console.log('getCurrentUser 响应:', JSON.stringify(currentUserRes, null, 2))
      userId = currentUserRes?.id
      if (!userId) {
        console.error('无法获取用户 ID')
        ElMessage.error('获取用户信息失败：无法获取用户ID')
        return
      }
    }

    // 第二步：使用 getUserDetail 获取完整用户信息（这会返回完整的 User 对象，包含所有字段）
    console.log('调用 getUserDetail, 用户ID:', userId)
    const response = await getUserDetail(userId)
    console.log('getUserDetail 原始响应:', JSON.stringify(response, null, 2))

    if (response) {
      const userData = response

      // 更新 adminInfo 用于显示
      Object.assign(adminInfo, {
        id: userData.id,
        username: userData.username || '',
        nickname: userData.nickname || '',
        avatar: userData.avatar || '',
        email: userData.email || '',
        phone: userData.phone || '',
        role: userData.role !== undefined ? userData.role : 0,
        userType: userData.userType || 2,
        status: userData.status || 1
      })

      console.log('adminInfo 已更新为:', JSON.stringify(adminInfo, null, 2))
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败：' + (error.message || ''))
  }
}

// 处理头像选择 - 直接上传（参照用户管理逻辑）
const handleAvatarSelect = async (file) => {
  try {
    console.log('选择头像文件:', file.name, '大小:', file.size)

    // 直接上传文件到MinIO
    const response = await uploadUserAvatar(file.raw)
    console.log('上传响应:', JSON.stringify(response, null, 2))

    if (response && response.originUrl) {
      profileForm.avatar = response.originUrl
      console.log('头像上传成功，URL:', profileForm.avatar)
      ElMessage.success('头像上传成功')
    } else {
      console.error('响应结构:', response)
      ElMessage.error('获取头像URL失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传头像失败')
  }
}

// 处理菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      openProfileDialog()
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 打开个人信息编辑对话框 - 参照用户管理的 handleEdit 逻辑
const openProfileDialog = async () => {
  // 先加载最新的用户信息
  await loadUserInfo()

  console.log('准备填充表单，adminInfo 当前值:', JSON.stringify(adminInfo, null, 2))

  // 然后填充表单数据 - 完全参照 handleEdit 逻辑
  profileForm.id = adminInfo.id
  profileForm.username = adminInfo.username || ''
  profileForm.nickname = adminInfo.nickname || ''
  profileForm.userType = adminInfo.userType || 2
  profileForm.role = adminInfo.role !== undefined ? adminInfo.role : 0
  profileForm.phone = adminInfo.phone || ''
  profileForm.email = adminInfo.email || ''
  profileForm.status = adminInfo.status || 1
  profileForm.avatar = adminInfo.avatar || '' // 保存当前头像URL，用于对比

  console.log('表单已填充为:', JSON.stringify(profileForm, null, 2))

  profileVisible.value = true
}

// 保存个人信息 - 参照用户管理的 handleSubmit 逻辑
const handleSaveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()

    savingProfile.value = true

    console.log('=== 开始提交个人信息 ===')

    // 编辑用户数据 - 参照用户管理的编辑逻辑
    const updateData = {
      id: profileForm.id,
      nickname: profileForm.nickname,
      userType: parseInt(profileForm.userType),
      role: parseInt(profileForm.role),
      phone: profileForm.phone,
      email: profileForm.email,
      status: profileForm.status
      // 默认不包含avatar字段
    }

    // 只有在头像改变时才提交新头像
    if (profileForm.avatar && profileForm.avatar !== adminInfo.avatar) {
      updateData.avatar = profileForm.avatar
      console.log('包含新头像')
    }

    console.log('提交的数据:', JSON.stringify(updateData, null, 2))

    await updateUser(updateData)
    console.log('个人信息保存成功')

    // 更新本地 adminInfo
    Object.assign(adminInfo, updateData)

    ElMessage.success('个人信息保存成功')
    profileVisible.value = false
  } catch (error) {
    console.error('保存个人信息失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    ElMessage.error(error.message || '保存失败')
  } finally {
    savingProfile.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功')
    showChangePassword.value = false
    Object.keys(passwordForm).forEach(key => {
      passwordForm[key] = ''
    })
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

// 保存系统设置
const handleSaveSettings = async () => {
  try {
    savingSettings.value = true
  ElMessage.success('设置保存成功')
  settingsVisible.value = false
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingSettings.value = false
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
  })
    .then(() => {
    try {
        authStore.resetState()
      
        localStorage.removeItem('user')
      localStorage.removeItem('permissions')
      sessionStorage.clear()
      
      ElMessage.success('退出成功')
      
        setTimeout(() => {
          router.push('/login').catch(err => {
            console.error('路由跳转失败:', err)
          })
        }, 300)
    } catch (error) {
      console.error('退出登录失败:', error)
      ElMessage.error('退出失败，请重试')
    }
    })
    .catch(() => {
    ElMessage.info('已取消退出')
  })
}

onMounted(async () => {
  try {
    // 先获取当前用户ID
    const currentUser = await getCurrentUser()
    if (currentUser && currentUser.id) {
      adminInfo.id = currentUser.id
      console.log('获取到用户ID:', adminInfo.id)
    }
  } catch (error) {
    console.error('获取当前用户ID失败:', error)
  }
  
  // 再加载完整用户信息
  loadUserInfo()
})
</script>

<style scoped>
.admin-avatar .avatar-wrapper {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 0 12px;
    height: 100%;
}
    
.admin-avatar .avatar-wrapper:hover {
      background-color: rgba(0, 0, 0, 0.02);
    }

.admin-avatar .avatar-wrapper .admin-name {
      margin: 0 8px;
      font-size: 16px;
      color: #606266;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  border: 1px dashed #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  color: #909399;
      font-size: 12px;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-icon) {
  margin-right: 4px;
}
</style> 
