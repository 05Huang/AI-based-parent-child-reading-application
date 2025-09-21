// 创建请求拦截器
const request = {
  // 全局配置
  baseUrl: 'http://localhost:8888',
  
  // 请求方法
  async request(options) {
    // 处理请求URL
    const url = `${this.baseUrl}${options.url}`;
    
    // 打印请求信息
    console.log(`发起${options.method}请求：${url}`, options.data || '');
    
    try {
      const response = await uni.request({
        url,
        method: options.method || 'GET',
        data: options.data,
        header: {
          'Content-Type': 'application/json',
          // 如果不是白名单接口且有token，添加到请求头
          ...(!options.url.match(/\/(login|register|smscode|send-email-code|check-email|check-phone|reset-password)/) && uni.getStorageSync('token') ? {
            'accessToken': uni.getStorageSync('token')
          } : {})
        }
      });
      
      // 打印响应信息
      console.log(`请求响应：`, response.data);
      
      // 处理响应
      if (response.statusCode === 200) {
        if (response.data.code === 200) {
          return response.data;
        } else {
          // 业务错误处理
          uni.showToast({
            title: response.data.msg || '请求失败',
            icon: 'none'
          });
          return Promise.reject(response.data);
        }
      } else {
        // HTTP错误处理
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
        return Promise.reject(response);
      }
    } catch (error) {
      console.error('请求出错：', error);
      uni.showToast({
        title: '网络请求失败',
        icon: 'none'
      });
      return Promise.reject(error);
    }
  },
  
  // GET请求
  get(url, data) {
    return this.request({
      url,
      method: 'GET',
      data
    });
  },
  
  // POST请求
  post(url, data) {
    return this.request({
      url,
      method: 'POST',
      data
    });
  }
};

export default request;
