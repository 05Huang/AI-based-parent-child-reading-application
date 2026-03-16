<template>
  <div class="error-container">
    <div class="error-box">
      <div class="error-code">403</div>
      <div class="error-title">权限不足</div>
      <div class="error-description">
        抱歉，您没有访问此页面的权限。请联系管理员获取相应权限。
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .error-box {
    background: white;
    border-radius: 12px;
    padding: 60px 40px;
    text-align: center;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    max-width: 500px;

    .error-code {
      font-size: 120px;
      font-weight: bold;
      color: #f56c6c;
      line-height: 1;
      margin-bottom: 10px;
    }

    .error-title {
      font-size: 32px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 20px;
    }

    .error-description {
      font-size: 16px;
      color: #606266;
      margin-bottom: 40px;
      line-height: 1.6;
    }

    .error-actions {
      display: flex;
      gap: 12px;
      justify-content: center;
      flex-wrap: wrap;

      .el-button {
        min-width: 120px;
      }
    }
  }
}
</style>









