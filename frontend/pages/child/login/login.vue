<template>
  <view class="container login-container">
    <view class="login-form">
      <view class="form-item">
        <input type="text" v-model="username" placeholder="请输入用户名" />
      </view>
      <view class="form-item">
        <input type="password" v-model="password" placeholder="请输入密码" />
      </view>
      <button type="primary" @click="handleLogin" style="background: #7C3AED;">登录</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const username = ref('')
const password = ref('')

const handleLogin = () => {
  // TODO: 实现登录逻辑
  console.log('孩子登录', { username: username.value, password: password.value })
}
</script>

<style>
.login-container {
  padding: 40rpx;
}

.login-form {
  margin-top: 100rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-item input {
  width: 100%;
  height: 88rpx;
  border: 1px solid #ddd;
  border-radius: 8rpx;
  padding: 0 20rpx;
}
</style> 