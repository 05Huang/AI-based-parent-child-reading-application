<template>
  <div class="page-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :inline="true" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select
            v-model="searchForm.userType"
            placeholder="请选择用户类型"
            clearable
            style="width: 200px"
          >
            <el-option label="普通用户" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="请选择角色"
            clearable
            style="width: 200px"
            :disabled="searchForm.userType === 2"
          >
            <el-option label="家长" :value="1" />
            <el-option label="孩子" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 200px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="RefreshRight" @click="handleReset">
            重置
          </el-button>
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            新增用户
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column width="60" align="center">
          <template #default="scope">
            <el-avatar :size="32" :src="scope.row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column prop="phone" label="手机号" width="120" align="center" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.userType === 2" type="primary" size="small">
              管理员
            </el-tag>
            <el-tag v-else :type="scope.row.role === 1 ? 'success' : 'warning'" size="small">
              {{ scope.row.role === 1 ? '家长' : '孩子' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button 
              link 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            placeholder="请输入用户名"
            :disabled="dialogType === 'edit'"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-upload">
            <el-image v-if="userForm.avatar" :src="userForm.avatar" style="width: 80px; height: 80px; border-radius: 4px" />
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
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="userForm.userType" placeholder="请选择用户类型" style="width: 100%" @change="handleUserTypeChange">
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%" :disabled="userForm.userType === 2">
            <el-option v-if="userForm.userType === 2" label="管理员" :value="0" />
            <el-option v-else-if="userForm.userType === 1" label="家长" :value="1" />
            <el-option v-else label="孩子" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  Search,
  RefreshRight,
  Plus,
  Delete,
  Edit
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getUserList, 
  addUser, 
  updateUser, 
  deleteUser, 
  updateUserStatus,
  checkUsernameAvailable,
  uploadUserAvatar,
  updateUserAvatar
} from '@/api/user'

// 搜索表单
const searchForm = reactive({
  username: '',
  userType: '',
  role: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 选中的行
const selectedRows = ref([])

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add')
const userFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  nickname: '',
  userType: 1,
  role: 1,
  phone: '',
  email: '',
  password: '',
  status: 1,
  avatar: '' // 只用于提交给后端的URL
})

// 显示用的头像（可能是base64预览或URL）
const displayAvatar = ref('')

// 头像上传相关
const handleAvatarChange = (file) => {
  // 这个函数不再需要，因为预览逻辑已移至 handleAvatarSelect
}

// 处理头像文件选择 - 选择后直接上传（参考家长端逻辑）
const handleAvatarSelect = async (file) => {
  try {
    console.log('选择头像文件:', file.name, '大小:', file.size)
    
    // 直接上传文件到MinIO
    const response = await uploadUserAvatar(file.raw)
    console.log('上传响应:', JSON.stringify(response, null, 2))
    
    // 获取返回的URL
    // request.js已经处理过响应，返回的就是data部分
    if (response && response.originUrl) {
      userForm.avatar = response.originUrl
      console.log('头像上传成功，URL:', userForm.avatar)
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

// 表单校验规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { 
      required: true, 
      message: '请输入密码', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (dialogType.value === 'add' && !value) {
          callback(new Error('新增用户必须设置密码'))
        } else {
          callback()
        }
      }
    },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 格式化时间
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取用户列表数据
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize,
      excludeAdminAndOfficial: true
    }
    
    // 添加搜索条件
    if (searchForm.username) {
      params.username = searchForm.username
    }
    if (searchForm.userType) {
      params.userType = parseInt(searchForm.userType)
    }
    if (searchForm.role) {
      params.role = parseInt(searchForm.role)
    }
    // 状态可能是0（禁用），所以不能用 !== ''，要用 != null
    if (searchForm.status != null) {
      const statusValue = parseInt(searchForm.status)
      // 检查parseInt是否返回NaN
      if (!isNaN(statusValue)) {
        params.status = statusValue
      }
    }
    
    const response = await getUserList(params)
    
    // 处理响应数据
    if (response && response.records) {
      tableData.value = response.records
      pagination.total = response.total
    } else if (Array.isArray(response)) {
      tableData.value = response
      pagination.total = response.length
    }
    
    ElMessage.success('获取用户列表成功')
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchUserList()
}

// 重置搜索表单
const handleReset = () => {
  searchForm.username = ''
  searchForm.userType = ''
  searchForm.role = ''
  searchForm.status = ''
  pagination.currentPage = 1
  fetchUserList()
}

// 页码改变
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchUserList()
}

// 每页条数改变
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchUserList()
}

// 初始化
onMounted(() => {
  fetchUserList()
})

const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  
  // 重置表单 - 更好的重置方式
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  
  // 重置对象属性
  userForm.id = null
  userForm.username = ''
  userForm.nickname = ''
  userForm.userType = 1
  userForm.role = 1
  userForm.phone = ''
  userForm.email = ''  // 确保邮箱为空
  userForm.password = ''
  userForm.status = 1
  userForm.avatar = ''
  displayAvatar.value = '' // 重置本地预览
  // avatarFile = null // 重置头像文件 - 不再需要
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  // 填充表单数据
  userForm.id = row.id
  userForm.username = row.username
  userForm.nickname = row.nickname || ''
  userForm.userType = row.userType || 1
  // 根据角色判断：如果role=0则为管理员（userType=2）
  if (row.role === 0 && row.userType === 2) {
    userForm.role = 0  // 管理员
    userForm.userType = 2
  } else {
    userForm.role = row.role
    userForm.userType = row.userType || 1
  }
  userForm.phone = row.phone || ''
  userForm.email = row.email || ''
  userForm.status = row.status
  userForm.avatar = row.avatar || '' // 填充头像URL（不是base64）
  displayAvatar.value = userForm.avatar // 填充本地预览
  // avatarFile = null // 重置头像文件，编辑时如果不重新上传就不提交avatar - 不再需要
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该用户吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        loading.value = true
        await deleteUser(row.id)
        ElMessage.success('删除成功')
        fetchUserList()
      } catch (error) {
        console.error('删除用户失败:', error)
        ElMessage.error('删除用户失败：' + error.message)
      } finally {
        loading.value = false
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(
    `确定要${statusText}该用户吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        loading.value = true
        await updateUserStatus(row.id, newStatus)
        ElMessage.success(`用户${statusText}成功`)
        fetchUserList()
      } catch (error) {
        console.error('更新用户状态失败:', error)
        ElMessage.error('更新用户状态失败：' + error.message)
      } finally {
        loading.value = false
      }
    })
    .catch(() => {
      ElMessage.info('已取消操作')
    })
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    
    loading.value = true
    
    console.log('=== 开始提交表单 ===')
    console.log('displayAvatar:', displayAvatar.value ? '有值（base64）' : '无值')
    console.log('userForm.avatar 提交前:', userForm.avatar)
    
    // 新增用户必须有头像，编辑用户可以不改头像
    if (dialogType.value === 'add' && !userForm.avatar) {
      ElMessage.error('请上传头像')
      loading.value = false
      return
    }
    
    console.log('userForm.avatar 最终值:', userForm.avatar)
    
    if (dialogType.value === 'add') {
      console.log('执行新增用户...')
      // 检查用户名是否已存在
      try {
        const result = await checkUsernameAvailable(userForm.username)
        if (!result) {
          ElMessage.error('用户名已存在')
          loading.value = false
          return
        }
      } catch (error) {
        console.warn('检查用户名失败，继续提交:', error)
      }
      
      // 新增用户
      await addUser({
        username: userForm.username,
        nickname: userForm.nickname,
        userType: parseInt(userForm.userType),
        role: parseInt(userForm.role),
        phone: userForm.phone,
        email: userForm.email,
        password: userForm.password,
        status: userForm.status,
        avatar: userForm.avatar
      })
      console.log('新增用户成功')
      ElMessage.success('添加成功')
    } else {
      console.log('执行编辑用户...')
      // 编辑用户 - 永远不要提交avatar字段，除非有新头像
      const updateData = {
        id: userForm.id,
        nickname: userForm.nickname,
        userType: parseInt(userForm.userType),
        role: parseInt(userForm.role),
        phone: userForm.phone,
        email: userForm.email,
        status: userForm.status
        // 默认不包含avatar字段
      }
      
      // 只有在userForm.avatar与原始avatar不同时才提交（说明是新头像）
      if (userForm.avatar) {
        updateData.avatar = userForm.avatar
        console.log('编辑用户包含新头像')
      }
      
      console.log('编辑用户数据:', JSON.stringify(updateData, null, 2))
      await updateUser(updateData)
      console.log('编辑用户成功')
      ElMessage.success('编辑成功')
    }
    
    dialogVisible.value = false
    fetchUserList()
    displayAvatar.value = '' // 重置本地预览
    // avatarFile = null // 重置头像文件 - 不再需要
  } catch (error) {
    console.error('表单验证或提交失败:', error)
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
  }
}

// 用户类型改变时触发
const handleUserTypeChange = () => {
  // 当用户类型变为管理员时，角色选项变为管理员
  if (userForm.userType === 2) {
    userForm.role = 0; // 管理员角色值
  } else {
    userForm.role = userForm.userType; // 普通用户或孩子角色值
  }
}
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
  height: 100%;
  background-color: #f5f7fa;

  .search-card {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);

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
    height: calc(100% - 140px);
    
    .el-table {
      height: calc(100% - 70px);
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-start;
  }
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;

  .avatar-placeholder {
    width: 80px;
    height: 80px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 14px;
    cursor: pointer;
  }

  .avatar-uploader {
    .el-upload {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      width: 80px;
      height: 80px;
    }

    .el-upload:hover {
      border-color: #409eff;
    }

    .el-icon--upload {
      font-size: 28px;
      color: #8c939d;
      margin-top: 4px;
    }
  }
}
</style> 