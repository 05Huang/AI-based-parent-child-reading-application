<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapse': isCollapse }">
      <div class="logo">
        <img src="../assets/logo.svg" alt="logo">
        <span v-show="!isCollapse">阅桥亲子阅读APP后台管理</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="route.path"
          :collapse="isCollapse"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :collapse-transition="false"
          router
        >
          <el-menu-item index="/dashboard-layout/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>控制台</template>
          </el-menu-item>
          <el-menu-item index="/dashboard-layout/user">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/dashboard-layout/articles">
            <el-icon><Document /></el-icon>
            <template #title>文章管理</template>
          </el-menu-item>
          <el-menu-item index="/dashboard-layout/videos">
            <el-icon><VideoCamera /></el-icon>
            <template #title>视频管理</template>
          </el-menu-item>
          <el-menu-item index="/dashboard-layout/comments">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>评论管理</template>
          </el-menu-item>
          <el-menu-item index="/dashboard-layout/chat-groups">
            <el-icon><ChatLineRound /></el-icon>
            <template #title>群聊管理</template>
          </el-menu-item>
          <el-menu-item index="/dashboard-layout/family">
            <el-icon><House /></el-icon>
            <template #title>家庭管理</template>
          </el-menu-item>
          <el-menu-item index="/dashboard-layout/questionnaire">
            <el-icon><DocumentCopy /></el-icon>
            <template #title>问卷管理</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </div>
    
    <!-- 主要内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="left">
          <el-icon class="fold-btn" @click="toggleSidebar">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard-layout/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right">
          <admin-avatar />
        </div>
      </div>
      
      <!-- 内容区 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand, Odometer, User, Document, VideoCamera, CaretBottom, ChatDotRound, ChatLineRound, House, DocumentCopy } from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import AdminAvatar from '@/components/AdminAvatar.vue'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const handleLogout = () => {
  // TODO: 实现登出逻辑
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  position: relative;
  height: 100vh;
  width: 100%;
  
  .sidebar-container {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    width: var(--sidebar-width);
    background-color: #304156;
    transition: width 0.3s;
    z-index: 1001;
    
    &.is-collapse {
      width: var(--sidebar-collapse-width);
    }
    
    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      padding: 0 16px;
      overflow: hidden;
      background: #2b3649;
      
      img {
        width: 64px;
        height: 64px;
        margin-right: 12px;
        flex-shrink: 0;
      }
      
      span {
        font-size: 16px;
        font-weight: 600;
        color: #fff;
        white-space: normal;
        line-height: 1.2;
      }
    }

    :deep(.el-menu) {
      border: none;
    }

    :deep(.el-scrollbar) {
      height: calc(100% - 60px);
      .el-menu-item {
        height: 56px;
        line-height: 56px;
        
        &.is-active {
          background-color: #263445;
        }
        
        .el-icon {
          font-size: 18px;
        }
      }
    }
  }
  
  .main-container {
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: var(--sidebar-width);
    height: 100vh;
    transition: left 0.3s;
    display: flex;
    flex-direction: column;
    z-index: 999;
    
    .navbar {
      position: relative;
      height: var(--navbar-height);
      background: #fff;
      box-shadow: 0 1px 4px rgba(0,21,41,.08);
      z-index: 1000;
      padding: 0 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-shrink: 0;
      
      .left {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .fold-btn {
          cursor: pointer;
          font-size: 20px;
          color: #606266;
          transition: color 0.3s;
          
          &:hover {
            color: #303133;
          }
        }
      }
      
      .right {
        display: flex;
        align-items: center;
        gap: 16px;
      }
    }
    
    .app-main {
      flex: 1;
      box-sizing: border-box;
      overflow-y: auto;
      overflow-x: hidden;
    }
  }

  &:has(.sidebar-container.is-collapse) {
    .main-container {
      left: var(--sidebar-collapse-width);
    }
  }
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>




