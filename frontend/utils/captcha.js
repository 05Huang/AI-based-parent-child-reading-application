/**
 * 腾讯云验证码工具类
 * 支持 H5 和 APP 环境
 */

class TencentCaptcha {
  constructor() {
    this.appId = '191227724' // 腾讯云验证码 AppId
    this.sdkLoaded = false
    this.loadingPromise = null
  }

  /**
   * 动态加载腾讯云验证码 SDK
   */
  loadSDK() {
    console.log('开始加载腾讯云验证码 SDK')
    
    // 如果已经在加载中，返回加载 Promise
    if (this.loadingPromise) {
      console.log('腾讯云验证码 SDK 正在加载中，等待加载完成')
      return this.loadingPromise
    }

    // 如果已经加载完成
    if (this.sdkLoaded && window.TencentCaptcha) {
      console.log('腾讯云验证码 SDK 已加载')
      return Promise.resolve()
    }

    // 开始加载 SDK
    this.loadingPromise = new Promise((resolve, reject) => {
      // 检查是否已经存在 script 标签
      const existingScript = document.querySelector('script[src*="TCaptcha.js"]')
      if (existingScript) {
        console.log('腾讯云验证码 SDK script 标签已存在')
        if (window.TencentCaptcha) {
          this.sdkLoaded = true
          console.log('腾讯云验证码 SDK 加载成功（已存在）')
          resolve()
        } else {
          // 等待脚本加载完成
          existingScript.onload = () => {
            this.sdkLoaded = true
            console.log('腾讯云验证码 SDK 加载成功（等待完成）')
            resolve()
          }
          existingScript.onerror = () => {
            console.error('腾讯云验证码 SDK 加载失败')
            reject(new Error('验证码 SDK 加载失败'))
          }
        }
        return
      }

      // 创建 script 标签加载 SDK
      const script = document.createElement('script')
      script.src = 'https://ssl.captcha.qq.com/TCaptcha.js'
      script.async = true
      
      script.onload = () => {
        this.sdkLoaded = true
        console.log('腾讯云验证码 SDK 加载成功（新加载）')
        resolve()
      }
      
      script.onerror = () => {
        console.error('腾讯云验证码 SDK 加载失败')
        this.loadingPromise = null
        reject(new Error('验证码 SDK 加载失败，请检查网络连接'))
      }
      
      document.head.appendChild(script)
      console.log('腾讯云验证码 SDK script 标签已添加到 head')
    })

    return this.loadingPromise
  }

  /**
   * 显示验证码
   * @returns {Promise} 返回验证结果 { ticket, randstr }
   */
  async show() {
    console.log('准备显示腾讯云验证码')
    
    try {
      // 确保 SDK 已加载
      await this.loadSDK()
      
      console.log('腾讯云验证码 SDK 已就绪，准备创建验证码实例')
      
      return new Promise((resolve, reject) => {
        // 创建验证码实例
        const captcha = new window.TencentCaptcha(this.appId, (res) => {
          console.log('腾讯云验证码回调结果：', res)
          
          // res.ret === 0 表示验证成功
          // res.ret === 2 表示用户主动关闭验证码
          if (res.ret === 0) {
            console.log('验证码验证成功')
            resolve({
              ticket: res.ticket,
              randstr: res.randstr
            })
          } else if (res.ret === 2) {
            console.log('用户关闭了验证码')
            reject(new Error('用户取消验证'))
          } else {
            console.error('验证码验证失败：', res)
            reject(new Error('验证失败，请重试'))
          }
        }, {})
        
        // 显示验证码
        console.log('显示验证码弹窗')
        captcha.show()
      })
    } catch (error) {
      console.error('显示验证码出错：', error)
      throw error
    }
  }

  /**
   * 获取当前运行环境
   * @returns {string} 'h5' | 'app-plus'
   */
  getPlatform() {
    // #ifdef H5
    return 'h5'
    // #endif
    
    // #ifdef APP-PLUS
    return 'app-plus'
    // #endif
    
    // 如果条件编译不生效，通过 uni 对象判断
    if (typeof uni !== 'undefined') {
      const systemInfo = uni.getSystemInfoSync()
      console.log('当前平台信息：', systemInfo.platform, systemInfo.uniPlatform)
      return systemInfo.uniPlatform || 'h5'
    }
    
    return 'h5'
  }

  /**
   * 检查当前环境是否支持验证码
   * @returns {boolean}
   */
  isSupported() {
    const platform = this.getPlatform()
    console.log('当前环境：', platform)
    
    // H5 和 APP 环境都支持
    // APP 环境通过 webview 使用 H5 验证码
    return platform === 'h5' || platform === 'app-plus'
  }
}

// 导出单例
const captchaInstance = new TencentCaptcha()

export default captchaInstance

