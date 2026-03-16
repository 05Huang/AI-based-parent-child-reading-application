<template>
  <view class="privacy-page" :style="themeStyle">
    <!-- 顶部导航栏 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="nav-title">隐私政策</text>
      </view>
    </view>

    <!-- 隐私政策内容 -->
    <scroll-view class="content-scroll" scroll-y>
      <view class="content-container">
        <view class="privacy-content">
          <text class="update-date">更新日期：2025年1月1日</text>
          <text class="effective-date">生效日期：2025年1月1日</text>

          <view class="section">
            <text class="section-title">引言</text>
            <text class="section-text">
              江苏师范大学智慧教育学院（以下简称"我们"）深知个人信息对您的重要性，我们将按照法律法规的规定，保护您的个人信息及隐私安全。我们制定本《隐私政策》并特别提示：希望您在使用阅桥亲子阅读APP应用及相关服务前仔细阅读并理解本隐私政策，以便做出适当的选择。
            </text>
          </view>

          <view class="section">
            <text class="section-title">一、我们如何收集和使用您的个人信息</text>
            <text class="section-text">
              个人信息是指以电子或者其他方式记录的能够单独或者与其他信息结合识别特定自然人身份或者反映特定自然人活动情况的各种信息。我们会出于以下目的收集和使用您的个人信息：
            </text>
            
            <text class="subsection-title">1.1 账号注册与登录</text>
            <text class="section-text">
              当您注册账号时，我们会收集您的手机号码、用户名、昵称、密码、头像、兴趣爱好等信息。这些信息用于创建您的账号，提供登录服务，以及为您推荐个性化内容。
            </text>

            <text class="subsection-title">1.2 提供核心功能</text>
            <text class="section-text">
              • 阅读服务：记录您的阅读进度、阅读历史、收藏内容等，为您提供更好的阅读体验。
            </text>
            <text class="section-text">
              • 互动功能：当您使用评论、点赞、分享等功能时，我们会收集相关信息。
            </text>
            <text class="section-text">
              • AI对话：您与AI助手的对话内容会被记录，用于改进服务质量。
            </text>
            <text class="section-text">
              • 亲子互动：家长与孩子的互动记录，用于生成亲密度统计。
            </text>

            <text class="subsection-title">1.3 改善我们的服务</text>
            <text class="section-text">
              我们可能会收集您的设备信息（如设备型号、操作系统版本）、日志信息（如访问时间、访问页面）等，用于分析应用使用情况，改进产品功能和用户体验。
            </text>

            <text class="subsection-title">1.4 推送通知</text>
            <text class="section-text">
              经您同意后，我们会向您推送阅读提醒、活动通知等信息。您可以在设置中关闭推送功能。
            </text>
          </view>

          <view class="section">
            <text class="section-title">二、我们如何使用Cookie和同类技术</text>
            <text class="section-text">
              2.1 为确保应用正常运转、为您提供更个性化的用户体验和服务，我们会使用Cookie和同类技术。
            </text>
            <text class="section-text">
              2.2 这些技术会收集您的浏览信息、偏好设置等，帮助您省去重复填写信息的步骤。
            </text>
            <text class="section-text">
              2.3 您可以通过浏览器或设备设置来管理或删除Cookie。但这可能会影响您使用我们的服务。
            </text>
          </view>

          <view class="section">
            <text class="section-title">三、我们如何共享、转让、公开披露您的个人信息</text>
            
            <text class="subsection-title">3.1 共享</text>
            <text class="section-text">
              我们不会与第三方共享您的个人信息，除非：
            </text>
            <text class="section-text indent">
              • 获得您的明确同意；
            </text>
            <text class="section-text indent">
              • 根据法律法规或政府部门的要求；
            </text>
            <text class="section-text indent">
              • 与我们的关联公司共享，用于提供服务；
            </text>
            <text class="section-text indent">
              • 与授权合作伙伴共享，仅限于提供服务所必需的信息。
            </text>

            <text class="subsection-title">3.2 转让</text>
            <text class="section-text">
              我们不会将您的个人信息转让给任何公司、组织和个人，但以下情况除外：
            </text>
            <text class="section-text indent">
              • 获得您的明确同意；
            </text>
            <text class="section-text indent">
              • 在涉及合并、收购或破产清算时，如涉及个人信息转让，我们会要求新的持有您个人信息的公司继续受本隐私政策的约束。
            </text>

            <text class="subsection-title">3.3 公开披露</text>
            <text class="section-text">
              我们仅会在以下情况下公开披露您的个人信息：
            </text>
            <text class="section-text indent">
              • 获得您的明确同意；
            </text>
            <text class="section-text indent">
              • 基于法律的披露。
            </text>
          </view>

          <view class="section">
            <text class="section-title">四、我们如何保护您的个人信息</text>
            <text class="section-text">
              4.1 我们采用符合业界标准的安全防护措施保护您的个人信息，防止数据遭到未经授权访问、公开披露、使用、修改、损坏或丢失。
            </text>
            <text class="section-text">
              4.2 我们采取以下措施保护您的个人信息：
            </text>
            <text class="section-text indent">
              • 数据加密传输（HTTPS）；
            </text>
            <text class="section-text indent">
              • 密码加密存储；
            </text>
            <text class="section-text indent">
              • 访问控制与权限管理；
            </text>
            <text class="section-text indent">
              • 定期安全审计。
            </text>
            <text class="section-text">
              4.3 尽管如此，请您理解，在互联网行业由于技术的限制和快速发展以及可能存在的各种恶意攻击手段，即使我们竭尽所能加强安全措施，也不可能始终保证信息的百分之百安全。
            </text>
          </view>

          <view class="section">
            <text class="section-title">五、您如何管理您的个人信息</text>
            <text class="section-text">
              您可以通过以下方式管理您的个人信息：
            </text>
            
            <text class="subsection-title">5.1 访问和更新您的信息</text>
            <text class="section-text">
              您可以在"我的"页面中查看和修改您的个人信息，包括头像、昵称、性别、生日等。
            </text>

            <text class="subsection-title">5.2 删除您的信息</text>
            <text class="section-text">
              您可以删除您发布的内容（如评论、阅读记录）。如需删除账号，请联系我们的客服。
            </text>

            <text class="subsection-title">5.3 改变您的授权</text>
            <text class="section-text">
              您可以在设备设置中改变您授予我们的权限（如相机、相册、通知等）。
            </text>

            <text class="subsection-title">5.4 注销账号</text>
            <text class="section-text">
              您可以通过联系客服申请注销账号。账号注销后，我们将停止为您提供服务，并根据法律规定删除您的个人信息。
            </text>
          </view>

          <view class="section">
            <text class="section-title">六、未成年人信息保护</text>
            <text class="section-text">
              6.1 我们非常重视未成年人的个人信息保护。如果您是未成年人，建议您请您的父母或监护人仔细阅读本隐私政策，并在征得您的父母或监护人同意的前提下使用我们的服务或向我们提供信息。
            </text>
            <text class="section-text">
              6.2 对于经父母或监护人同意而收集未成年人个人信息的情况，我们只会在受到法律允许、父母或监护人明确同意或者保护未成年人所必要的情况下使用或公开披露此信息。
            </text>
            <text class="section-text">
              6.3 如果我们发现在未事先获得可证实的父母同意的情况下收集了未成年人的个人信息，则会设法尽快删除相关数据。
            </text>
          </view>

          <view class="section">
            <text class="section-title">七、隐私政策的更新</text>
            <text class="section-text">
              7.1 我们可能会适时修订本隐私政策。当隐私政策发生变更时，我们会在应用中以推送通知、弹窗等形式向您展示变更后的内容。
            </text>
            <text class="section-text">
              7.2 未经您明确同意，我们不会削减您按照本隐私政策所应享有的权利。
            </text>
            <text class="section-text">
              7.3 对于重大变更，我们还会提供更为显著的通知。
            </text>
          </view>

          <view class="section">
            <text class="section-title">八、如何联系我们</text>
            <text class="section-text">
              如果您对本隐私政策有任何疑问、意见或建议，或您在使用服务过程中遇到个人信息相关的问题，您可以通过以下方式与我们联系：
            </text>
            <text class="section-text">
              联系邮箱：privacy@parentreading.com
            </text>
            <text class="section-text">
              联系地址：江苏省徐州市铜山区上海路101号江苏师范大学智慧教育学院
            </text>
            <text class="section-text">
              一般情况下，我们将在15个工作日内回复您的请求。
            </text>
          </view>

          <view class="section">
            <text class="section-text">
              感谢您对我们的信任！我们将始终致力于保护您的个人信息安全。
            </text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

console.log('隐私政策页面加载')

const themeStyle = computed(() => {
  let role = null
  try {
    const info = uni.getStorageSync('userInfo') || {}
    role = info.role
    if (!role) {
      const storedRole = uni.getStorageSync('userRole')
      role = storedRole
    }
  } catch (e) {}
  const isChild = role === 2 || role === 'child'
  return { '--primary-color': isChild ? '#8b5cf6' : '#3b82f6' }
})

const goBack = () => {
  console.log('返回上一页')
  uni.navigateBack({
    delta: 1
  })
}
</script>

<style scoped>
/* 引入 Font Awesome */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.privacy-page {
  width: 100%;
  height: 100vh;
  background-color: #F9FAFB;
  display: flex;
  flex-direction: column;
}

.nav-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background-color: var(--primary-color);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-content {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
}

.back-btn {
  font-size: 36rpx;
  color: #FFFFFF;
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #FFFFFF;
  margin-left: 16rpx;
}

.content-scroll {
  flex: 1;
  margin-top: 128rpx;
}

.content-container {
  padding: 32rpx;
}

.privacy-content {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 40rpx;
}

.update-date, .effective-date {
  display: block;
  font-size: 24rpx;
  color: #9CA3AF;
  margin-bottom: 8rpx;
}

.section {
  margin-top: 48rpx;
}

.section:first-of-type {
  margin-top: 32rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #1F2937;
  margin-bottom: 24rpx;
}

.subsection-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #374151;
  margin-top: 24rpx;
  margin-bottom: 16rpx;
}

.section-text {
  display: block;
  font-size: 28rpx;
  color: #4B5563;
  line-height: 48rpx;
  margin-bottom: 16rpx;
}

.section-text.indent {
  padding-left: 32rpx;
  color: #6B7280;
}
</style>

