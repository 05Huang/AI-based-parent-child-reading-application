<template>
  <view class="agreement-page">
    <!-- 顶部导航栏 -->
    <view class="nav-header">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <text class="fas fa-arrow-left"></text>
        </view>
        <text class="nav-title">用户协议</text>
      </view>
    </view>

    <!-- 协议内容 -->
    <scroll-view class="content-scroll" scroll-y>
      <view class="content-container">
        <view class="agreement-content">
          <text class="update-date">更新日期：2025年1月1日</text>
          <text class="effective-date">生效日期：2025年1月1日</text>

          <view class="section">
            <text class="section-title">欢迎使用阅桥亲子阅读APP应用</text>
            <text class="section-text">
              感谢您选择阅桥亲子阅读APP应用（以下简称"本应用"或"我们"）。在使用本应用之前，请您仔细阅读并理解本《用户协议》（以下简称"本协议"）。本协议是您与江苏师范大学智慧教育学院之间关于您使用本应用服务的法律协议。
            </text>
          </view>

          <view class="section">
            <text class="section-title">一、服务说明</text>
            <text class="section-text">
              1.1 本应用是一款专注于阅桥亲子阅读APP的移动应用程序，致力于为家长和孩子提供优质的阅读内容和互动体验。
            </text>
            <text class="section-text">
              1.2 本应用提供的服务包括但不限于：图文内容阅读、视频内容观看、AI智能对话、家长与孩子的互动交流等功能。
            </text>
            <text class="section-text">
              1.3 我们保留随时修改、中断或终止部分或全部服务的权利，恕不另行通知。
            </text>
          </view>

          <view class="section">
            <text class="section-title">二、用户注册与账号</text>
            <text class="section-text">
              2.1 您需要注册账号才能使用本应用的完整功能。注册时，您应提供真实、准确、完整的个人信息。
            </text>
            <text class="section-text">
              2.2 您应妥善保管账号和密码，对该账号下的所有活动承担责任。如发现账号被盗用，应立即通知我们。
            </text>
            <text class="section-text">
              2.3 一个手机号只能注册一个账号。您不得将账号转让、出借或出售给他人使用。
            </text>
            <text class="section-text">
              2.4 如果您提供的信息不准确或违反本协议，我们有权暂停或终止您的账号。
            </text>
          </view>

          <view class="section">
            <text class="section-title">三、用户行为规范</text>
            <text class="section-text">
              3.1 您在使用本应用时，应遵守中华人民共和国相关法律法规，不得利用本应用从事违法违规活动。
            </text>
            <text class="section-text">
              3.2 您不得发布、传播含有以下内容的信息：
            </text>
            <text class="section-text indent">
              • 反对宪法所确定的基本原则的；
            </text>
            <text class="section-text indent">
              • 危害国家安全，泄露国家秘密的；
            </text>
            <text class="section-text indent">
              • 煽动民族仇恨、民族歧视的；
            </text>
            <text class="section-text indent">
              • 散布谣言，扰乱社会秩序的；
            </text>
            <text class="section-text indent">
              • 侵犯他人合法权益的；
            </text>
            <text class="section-text indent">
              • 含有暴力、恐怖、色情等不良内容的；
            </text>
            <text class="section-text indent">
              • 其他违反法律法规的内容。
            </text>
            <text class="section-text">
              3.3 您不得干扰或破坏本应用的正常运行，不得攻击、侵入本应用的服务器或网络。
            </text>
            <text class="section-text">
              3.4 您应尊重其他用户，不得发布骚扰、辱骂、诽谤他人的信息。
            </text>
          </view>

          <view class="section">
            <text class="section-title">四、知识产权</text>
            <text class="section-text">
              4.1 本应用的所有内容，包括但不限于文字、图片、视频、音频、软件、程序、界面设计等，均受著作权法、商标法等法律法规保护。
            </text>
            <text class="section-text">
              4.2 未经我们书面许可，您不得擅自复制、修改、传播本应用的内容，不得用于商业用途。
            </text>
            <text class="section-text">
              4.3 您上传的内容（如评论、图片等）的知识产权归您所有，但您授予我们在本应用范围内免费使用该内容的权利。
            </text>
          </view>

          <view class="section">
            <text class="section-title">五、未成年人保护</text>
            <text class="section-text">
              5.1 本应用非常重视未成年人的保护。如果您是未成年人，请在家长或监护人的指导下使用本应用。
            </text>
            <text class="section-text">
              5.2 家长或监护人应监督未成年人使用本应用的行为，确保其合理使用。
            </text>
            <text class="section-text">
              5.3 我们采取技术措施，过滤不适合未成年人的内容，为未成年人营造健康、安全的网络环境。
            </text>
          </view>

          <view class="section">
            <text class="section-title">六、免责声明</text>
            <text class="section-text">
              6.1 本应用按"现状"提供服务，不保证服务不会中断或无错误。
            </text>
            <text class="section-text">
              6.2 因不可抗力（如自然灾害、战争等）或其他非我们可控的原因导致的服务中断或数据丢失，我们不承担责任。
            </text>
            <text class="section-text">
              6.3 您应对使用本应用产生的风险自行承担责任。
            </text>
          </view>

          <view class="section">
            <text class="section-title">七、协议的修改与终止</text>
            <text class="section-text">
              7.1 我们有权随时修改本协议。修改后的协议将在本应用上公布，并自公布之日起生效。
            </text>
            <text class="section-text">
              7.2 如果您不同意修改后的协议，应立即停止使用本应用。继续使用本应用，视为您接受修改后的协议。
            </text>
            <text class="section-text">
              7.3 如果您违反本协议，我们有权暂停或终止您的账号，并保留追究法律责任的权利。
            </text>
          </view>

          <view class="section">
            <text class="section-title">八、法律适用与争议解决</text>
            <text class="section-text">
              8.1 本协议的订立、执行、解释及争议解决均适用中华人民共和国法律。
            </text>
            <text class="section-text">
              8.2 如就本协议内容或其执行发生争议，双方应友好协商解决；协商不成时，任何一方均可向本应用所在地人民法院提起诉讼。
            </text>
          </view>

          <view class="section">
            <text class="section-title">九、联系我们</text>
            <text class="section-text">
              如您对本协议有任何疑问，或需要与我们联系，请通过以下方式：
            </text>
            <text class="section-text">
              联系邮箱：support@parentreading.com
            </text>
            <text class="section-text">
              联系地址：江苏省徐州市铜山区上海路101号江苏师范大学智慧教育学院
            </text>
          </view>

          <view class="section">
            <text class="section-text">
              再次感谢您使用阅桥亲子阅读APP应用！
            </text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
console.log('用户协议页面加载')

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

.agreement-page {
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
  background-color: #FFFFFF;
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
  color: #666;
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1F2937;
  margin-left: 16rpx;
}

.content-scroll {
  flex: 1;
  margin-top: 128rpx;
}

.content-container {
  padding: 32rpx;
}

.agreement-content {
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

