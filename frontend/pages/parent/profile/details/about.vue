# 创建关于我们页面的基本模板
<template>
  <view class="container">
    <!-- 顶部导航 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left text-gray-600"></text>
        </view>
        <text class="nav-title">关于我们</text>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <scroll-view scroll-y="true" class="main-content">
      <!-- Logo和标题 -->
      <view class="header-section">
        <view class="logo-box">
          <text class="fa-solid fa-book-reader"></text>
        </view>
        <text class="platform-name">基于大模型的大学生亲子共读平台</text>
      </view>

      <!-- 团队介绍 -->
      <view class="content-section">
        <view class="section-header">
          <text class="fas fa-users section-icon"></text>
          <text class="section-title">团队介绍</text>
        </view>
        <view class="section-content">
          <text class="content-text">我们是一群来自江苏师范大学的大学生团队，致力于通过AI技术重构大学生与父母的情感连接方式。团队依托智慧教育学院的技术资源，聚焦于解决当代大学生与父母因地理距离、生活节奏差异导致的沟通疏离问题，打造了以"大模型驱动亲子共读"为核心的心理健康辅助平台。</text>
        </view>
      </view>

      <!-- 我们的初心 -->
      <view class="content-section">
        <view class="section-header">
          <text class="fas fa-heart section-icon"></text>
          <text class="section-title">我们的初心</text>
        </view>
        <view class="section-content">
          <text class="section-subtitle">让陪伴突破时空边界</text>
          <text class="content-text">调研数据显示，全国约22.8%的大学生存在心理障碍，而父母消极的教养方式或陪伴缺失是重要诱因。我们发现，亲子共读作为一种高参与度的互动形式，能有效提升大学生幸福感，但现实中面临"父母时间不足、沟通延迟、情感表达隔阂"等痛点。因此，团队构建了兼具个性化推荐、AI情感回复、悬赏互动机制的共读平台，让父母的陪伴通过文字与AI技术自然融入孩子的大学生活。</text>
        </view>
      </view>

      <!-- 我们的技术 -->
      <view class="content-section">
        <view class="section-header">
          <text class="fas fa-microchip section-icon"></text>
          <text class="section-title">我们的技术</text>
        </view>
        <view class="section-content">
          <text class="section-subtitle">用AI模拟"有温度的陪伴"</text>
          <view class="tech-features">
            <view class="tech-item">
              <text class="fas fa-robot tech-icon"></text>
              <text class="tech-text">AI情感对话</text>
            </view>
            <view class="tech-item">
              <text class="fas fa-brain tech-icon"></text>
              <text class="tech-text">大模型驱动</text>
            </view>
            <view class="tech-item">
              <text class="fas fa-chart-line tech-icon"></text>
              <text class="tech-text">智能分析</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 我们的愿景 -->
      <view class="content-section">
        <view class="section-header">
          <text class="fas fa-star section-icon"></text>
          <text class="section-title">我们的愿景</text>
        </view>
        <view class="section-content">
          <text class="section-subtitle">成为心理健康教育的数字桥梁</text>
          <text class="content-text">我们以"提高父母陪伴作用，加强大学生生活幸福度"为宗旨，不仅希望通过订阅服务、教育合作实现可持续运营，更致力于推动心理健康辅助体系的数字化升级。未来，平台将拓展"家庭心理档案""跨代际话题库"等功能，联合高校心理研究机构，让AI技术真正成为连接亲情、守护大学生心理健康的纽带，助力培养兼具专业能力与心理韧性的社会主义建设者。</text>
        </view>
      </view>

      <!-- 团队与支持 -->
      <view class="content-section">
        <view class="section-header">
          <text class="fas fa-handshake section-icon"></text>
          <text class="section-title">团队与支持</text>
        </view>
        <view class="section-content">
          <text class="content-text">项目由软件工程、教育技术学专业学生组成，副教授宋媚担任指导教师。我们始终相信，技术的温度在于解决真实的社会需求——而父母的一句"我在"，或许就是大学生面对挑战时最坚实的力量。</text>
        </view>
      </view>

      <!-- 联系我们 -->
      <view class="contact-section">
        <view class="contact-item">
          <text class="fas fa-envelope contact-icon"></text>
          <text class="contact-text">联系邮箱：contact@example.com</text>
        </view>
        <view class="contact-item">
          <text class="fas fa-map-marker-alt contact-icon"></text>
          <text class="contact-text">地址：江苏师范大学</text>
        </view>
      </view>

      <!-- 版本信息 -->
      <view class="version-info">
        <text class="version-text">版本 1.0.0</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
// 返回上一页
const goBack = () => {
  uni.navigateBack()
}
</script>

<style>
/* 引入Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.container {
  min-height: 100vh;
  background-color: #f3f4f6;
  width: 100%;
  box-sizing: border-box;
}

/* 顶部导航样式 */
.nav-header {
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
}

.nav-content {
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
}

.back-btn {
  padding: 20rpx;
  margin: -20rpx;
  margin-right: 10rpx;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
}

/* 主要内容区域 */
.main-content {
  margin-top: 88rpx;
  padding: 30rpx;
  height: calc(100vh - 88rpx);
  width: 100%;
  box-sizing: border-box;
}

/* Logo和标题样式 */
.header-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;
  padding: 40rpx 0;
  background-color: #ffffff;
  border-radius: 16rpx;
}

.logo-box {
  width: 160rpx;
  height: 160rpx;
  background-color: rgb(59, 130, 246);
  border-radius: 32rpx;
  margin: 0 auto 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-box .fa-book-reader {
  color: #FFFFFF;
  font-size: 48rpx;
}

.platform-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #111827;
  text-align: center;
}

/* 内容区块样式 */
.content-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-icon {
  font-size: 36rpx;
  color: #3b82f6;
  margin-right: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #111827;
}

.section-subtitle {
  font-size: 28rpx;
  font-weight: bold;
  color: #4b5563;
  margin-bottom: 16rpx;
  display: block;
}

.content-text {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.6;
}

/* 技术特点样式 */
.tech-features {
  display: flex;
  justify-content: space-around;
  margin-top: 30rpx;
}

.tech-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.tech-icon {
  font-size: 48rpx;
  color: #3b82f6;
}

.tech-text {
  font-size: 24rpx;
  color: #4b5563;
}

/* 联系方式样式 */
.contact-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.contact-item:last-child {
  margin-bottom: 0;
}

.contact-icon {
  font-size: 32rpx;
  color: #3b82f6;
  margin-right: 16rpx;
  width: 40rpx;
  text-align: center;
}

.contact-text {
  font-size: 28rpx;
  color: #4b5563;
}

/* 版本信息样式 */
.version-info {
  text-align: center;
  padding: 30rpx 0;
}

.version-text {
  font-size: 24rpx;
  color: #9ca3af;
}

/* 修复滚动问题 */
::-webkit-scrollbar {
  display: none;
}
</style> 