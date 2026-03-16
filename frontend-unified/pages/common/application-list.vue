<template>
  <view class="container" :style="themeStyle">
    <view class="status-bar"></view>
    <view class="nav-header">
      <view class="back-btn" @click="goBack">
        <text class="fas fa-chevron-left"></text>
      </view>
      <text class="title">申请列表</text>
      <view class="right-placeholder"></view>
    </view>

    <view class="content">
      <view v-if="applications.length === 0" class="empty-state">
        <text class="fas fa-inbox empty-icon"></text>
        <text class="empty-text">暂无新的申请</text>
      </view>
      
      <view v-else class="list-container">
        <view v-for="(item, index) in applications" :key="index" class="application-item">
          <image :src="item.avatar" class="avatar" mode="aspectFill"></image>
          <view class="info">
            <view class="name-row">
              <text class="name">{{ item.name }}</text>
              <text class="time">{{ item.time }}</text>
            </view>
            <text class="message">{{ item.message }}</text>
          </view>
          <view class="action-btn" :class="{ 'accepted': item.status === 'accepted' }" @click="handleAction(item)">
            <text>{{ item.status === 'pending' ? '同意' : '已同意' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app'
import { friendApi, userApi, groupApi } from '@/utils/api.js';

const applications = ref([]);
const currentUser = ref(null);
const HISTORY_KEY = 'groupInviteHistory';

const loadInviteHistory = () => {
  try {
    const list = uni.getStorageSync(HISTORY_KEY)
    return Array.isArray(list) ? list : []
  } catch (e) {
    return []
  }
}

const saveInviteHistoryItem = (item) => {
  try {
    const list = loadInviteHistory()
    const idx = list.findIndex(x => x.id === item.id)
    const record = {
      id: item.id,
      userId: item.userId,
      name: item.name,
      avatar: item.avatar,
      message: item.message,
      time: item.time,
      status: 'accepted',
      type: 'group_invite',
      createdAt: item.createdAt || null
    }
    if (idx >= 0) {
      list[idx] = record
    } else {
      list.push(record)
    }
    uni.setStorageSync(HISTORY_KEY, list)
  } catch (e) {
  }
}

const loadCurrentUser = async () => {
  try {
    const res = await userApi.getCurrentUser();
    if (res && res.code === 200) {
      currentUser.value = res.data;
      loadApplications();
    }
  } catch (e) {
    console.error('获取用户信息失败', e);
  }
};

const loadApplications = async () => {
  if (!currentUser.value) return;
  try {
    uni.showLoading({ title: '加载中...' });
    const [fr, gi] = await Promise.allSettled([
      friendApi.getFriendRequests(),
      groupApi.getGroupInvites()
    ])
    if (fr.status === 'fulfilled') {
      console.log('好友申请原始数据：', fr.value)
    } else {
      console.error('好友申请接口失败：', fr.reason)
    }
    if (gi.status === 'fulfilled') {
      console.log('群聊邀请原始数据：', gi.value)
    } else {
      console.error('群聊邀请接口失败：', gi.reason)
    }
    const friendList = (fr.status === 'fulfilled' && fr.value && fr.value.code === 200 && Array.isArray(fr.value.data)) ? fr.value.data : [];
    const groupInvites = (gi.status === 'fulfilled' && gi.value && gi.value.code === 200 && Array.isArray(gi.value.data)) ? gi.value.data : [];
    const friends = friendList.map(item => ({
        id: item.id, // 申请ID
        userId: item.userId, // 申请人ID
        name: item.nickname || item.username || '未知用户',
        avatar: item.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + (item.userId || item.id),
        message: item.remark || item.note || item.message || '申请添加你为好友',
        time: formatTime(item.createdTime),
        status: item.status === 1 ? 'accepted' : (item.status === 2 ? 'rejected' : 'pending'),
        type: 'friend'
      }))
    const ginvRaw = groupInvites.map(item => ({
      id: item.id,
      userId: item.groupId,
      name: item.groupName || '群聊邀请',
      avatar: item.groupAvatar || item.avatar || item.groupAvatarUrl || ('https://ui-avatars.com/api/?name=' + encodeURIComponent(item.groupName || '群聊') + '&background=3b82f6&color=fff&size=128'),
      message: item.message || '邀请你加入群聊',
      time: formatTime(item.createdTime),
      status: item.status === 1 ? 'accepted' : (item.status === 2 ? 'rejected' : 'pending'),
      type: 'group_invite',
      createdAt: item.createdTime || null
    }))
    const history = loadInviteHistory()
    const byId = new Map()
    ginvRaw.forEach(it => byId.set(it.id, it))
    history.forEach(h => byId.set(h.id, h))
    const ginv = Array.from(byId.values())
    applications.value = [...ginv, ...friends]
  } catch (e) {
    console.error('获取申请列表失败', e);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
};

const formatTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false });
  }
  return (date.getMonth() + 1) + '-' + date.getDate();
};

const goBack = () => {
  uni.navigateBack();
};

const handleAction = async (item) => {
  if (item.status === 'pending') {
    uni.showModal({
      title: '提示',
      content: `确定同意 ${item.name} 的${item.type === 'group_invite' ? '邀请' : '申请'}吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            uni.showLoading({ title: '处理中...' });
            const apiRes = item.type === 'group_invite'
              ? await groupApi.handleGroupInvite(item.id, 1)
              : await friendApi.handleFriendRequest(item.id, 1);
            if (apiRes && apiRes.code === 200) {
                item.status = 'accepted';
                if (item.type === 'group_invite') {
                  saveInviteHistoryItem(item)
                }
                uni.showToast({
                    title: '已同意',
                    icon: 'success'
                });
            } else {
                uni.showToast({ title: apiRes.msg || '操作失败', icon: 'none' });
            }
          } catch (e) {
            console.error(e);
            uni.showToast({ title: '操作失败', icon: 'none' });
          } finally {
            uni.hideLoading();
          }
        }
      }
    });
  }
};

onMounted(() => {
  loadCurrentUser();
});

onShow(() => {
  if (currentUser.value) {
    loadApplications()
  } else {
    loadCurrentUser()
  }
})

const themeStyle = computed(() => {
  const role = currentUser.value?.role
  const isChild = role === 2
  return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
})
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #F8F8F8;
  display: flex;
  flex-direction: column;
}

.status-bar {
  height: var(--status-bar-height);
  background-color: #fff;
}

.nav-header {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background-color: var(--primary-color);
  position: sticky;
  top: var(--status-bar-height);
  z-index: 100;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.back-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.right-placeholder {
  width: 32px;
}

.content {
  flex: 1;
  padding: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.application-item {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #eee;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.message {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-btn {
  padding: 6px 16px;
  background-color: #4A90E2;
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  transition: opacity 0.2s;
}

.action-btn:active {
  opacity: 0.8;
}

.action-btn.accepted {
  background-color: #f0f0f0;
  color: #999;
}
</style>
