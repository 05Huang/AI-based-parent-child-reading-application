/**
 * 阅读模式管理器
 * 负责管理阅读模式的设置和主题应用
 */

// 存储键名
const STORAGE_KEY = 'reading_mode_settings'

// 主题配置
export const themes = [
  { 
    id: 'default', 
    name: '默认',
    backgroundColor: '#ffffff',
    textColor: '#111827',
    headerBgColor: '#3b82f6'
  },
  { 
    id: 'sepia', 
    name: '护眼',
    backgroundColor: '#f8f3e8',
    textColor: '#5c4b37',
    headerBgColor: '#d4a574'
  },
  { 
    id: 'dark', 
    name: '深色',
    backgroundColor: '#1a1a1a',
    textColor: '#e5e7eb',
    headerBgColor: '#2d3748'
  },
  { 
    id: 'green', 
    name: '绿色',
    backgroundColor: '#f0fdf4',
    textColor: '#166534',
    headerBgColor: '#10b981'
  }
]

// 默认设置
export const defaultSettings = {
  theme: 'default',
  fontSize: 16,
  lineHeight: 1.8,
  landscapeLocked: false,
  keepScreenOn: true,
  pageAnimation: true
}

/**
 * 保存阅读模式设置
 * @param {Object} settings - 设置对象
 */
export const saveSettings = (settings) => {
  console.log('保存阅读模式设置:', settings)
  try {
    uni.setStorageSync(STORAGE_KEY, settings)
    console.log('阅读模式设置保存成功')
    return true
  } catch (error) {
    console.error('保存阅读模式设置失败:', error)
    return false
  }
}

/**
 * 读取阅读模式设置
 * @returns {Object} 设置对象
 */
export const loadSettings = () => {
  console.log('读取阅读模式设置')
  try {
    const settings = uni.getStorageSync(STORAGE_KEY)
    if (settings) {
      console.log('读取到已保存的设置:', settings)
      // 合并默认设置，确保所有字段都存在
      return { ...defaultSettings, ...settings }
    } else {
      console.log('未找到已保存的设置，使用默认设置')
      // 首次使用，保存默认设置
      saveSettings(defaultSettings)
      return { ...defaultSettings }
    }
  } catch (error) {
    console.error('读取阅读模式设置失败:', error)
    return { ...defaultSettings }
  }
}

/**
 * 获取主题配置
 * @param {String} themeId - 主题ID
 * @returns {Object} 主题配置对象
 */
export const getThemeById = (themeId) => {
  return themes.find(t => t.id === themeId) || themes[0]
}

/**
 * 应用阅读模式设置到页面
 * @param {Object} settings - 设置对象
 * @param {Object} pageContext - 页面上下文(可选)
 */
export const applySettings = (settings, pageContext = null) => {
  console.log('应用阅读模式设置:', settings)
  
  // 应用主题
  const theme = getThemeById(settings.theme)
  applyTheme(theme)
  
  // 应用字体大小和行高
  applyFontSettings(settings.fontSize, settings.lineHeight)
  
  // 应用字体家族
  applyFontFamily()
  
  // 应用屏幕常亮设置
  if (settings.keepScreenOn !== undefined) {
    applyKeepScreenOn(settings.keepScreenOn)
  }
  
  // 应用横屏锁定(如果支持)
  if (settings.landscapeLocked !== undefined) {
    applyScreenOrientation(settings.landscapeLocked)
  }
  
  console.log('阅读模式设置应用完成')
}

/**
 * 应用主题
 * @param {Object} theme - 主题对象
 */
export const applyTheme = (theme) => {
  console.log('应用主题:', theme.name)
  
  try {
    // 通过CSS变量应用主题
    const root = document.documentElement
    root.style.setProperty('--reading-bg-color', theme.backgroundColor)
    root.style.setProperty('--reading-text-color', theme.textColor)
    root.style.setProperty('--reading-header-bg', theme.headerBgColor)
    
    // 根据主题设置标题颜色
    const isDark = theme.id === 'dark'
    const titleColor = isDark ? '#f3f4f6' : '#1f2937'
    const metaColor = isDark ? '#9ca3af' : '#6b7280'
    root.style.setProperty('--reading-title-color', titleColor)
    root.style.setProperty('--reading-meta-color', metaColor)
    
    // 根据主题设置悬停和按钮背景色
    if (isDark) {
      // 深色主题
      root.style.setProperty('--reading-hover-bg', 'rgba(255, 255, 255, 0.05)')
      root.style.setProperty('--reading-btn-bg', 'rgba(255, 255, 255, 0.08)')
      root.style.setProperty('--reading-btn-hover-bg', 'rgba(255, 255, 255, 0.12)')
      root.style.setProperty('--reading-comment-bg', 'rgba(59, 130, 246, 0.15)')
      root.style.setProperty('--reading-comment-btn-bg', 'rgba(59, 130, 246, 0.25)')
      root.style.setProperty('--reading-card-bg', 'rgba(45, 45, 45, 0.95)') // 表情选择器等卡片背景
      root.style.setProperty('--reading-border-color', '#404040') // 分隔线颜色
      root.style.setProperty('--reading-active-icon', '#60a5fa') // 激活状态图标颜色
      root.style.setProperty('--reading-back-to-top-bg', 'linear-gradient(135deg, #60a5fa, #3b82f6)') // 回到顶部按钮背景
      root.style.setProperty('--reading-back-to-top-shadow', 'rgba(96, 165, 250, 0.3)') // 回到顶部按钮阴影
      root.style.setProperty('--reading-back-to-top-shadow-hover', 'rgba(96, 165, 250, 0.4)') // 回到顶部按钮悬停阴影
      root.style.setProperty('--reading-tip-bg', 'linear-gradient(135deg, #60a5fa, #3b82f6)') // 功能提示背景
      root.style.setProperty('--reading-tip-shadow', 'rgba(96, 165, 250, 0.2)') // 功能提示阴影
      // 底部工具栏
      root.style.setProperty('--reading-toolbar-bg', '#2d2d2d')
      root.style.setProperty('--reading-toolbar-border', '#404040')
      root.style.setProperty('--reading-toolbar-text', '#d1d5db')
      root.style.setProperty('--reading-toolbar-icon', '#9ca3af')
    } else if (theme.id === 'sepia') {
      // 护眼主题
      root.style.setProperty('--reading-hover-bg', 'rgba(92, 75, 55, 0.05)')
      root.style.setProperty('--reading-btn-bg', 'rgba(92, 75, 55, 0.08)')
      root.style.setProperty('--reading-btn-hover-bg', 'rgba(92, 75, 55, 0.12)')
      root.style.setProperty('--reading-comment-bg', 'rgba(212, 165, 116, 0.15)')
      root.style.setProperty('--reading-comment-btn-bg', 'rgba(212, 165, 116, 0.25)')
      root.style.setProperty('--reading-card-bg', 'rgba(245, 238, 217, 0.95)') // 表情选择器等卡片背景
      root.style.setProperty('--reading-border-color', '#e8dcc0') // 分隔线颜色
      root.style.setProperty('--reading-active-icon', '#d4a574') // 激活状态图标颜色
      root.style.setProperty('--reading-back-to-top-bg', 'linear-gradient(135deg, #d4a574, #b8935f)') // 回到顶部按钮背景
      root.style.setProperty('--reading-back-to-top-shadow', 'rgba(212, 165, 116, 0.3)') // 回到顶部按钮阴影
      root.style.setProperty('--reading-back-to-top-shadow-hover', 'rgba(212, 165, 116, 0.4)') // 回到顶部按钮悬停阴影
      root.style.setProperty('--reading-tip-bg', 'linear-gradient(135deg, #d4a574, #b8935f)') // 功能提示背景
      root.style.setProperty('--reading-tip-shadow', 'rgba(212, 165, 116, 0.2)') // 功能提示阴影
      // 底部工具栏
      root.style.setProperty('--reading-toolbar-bg', '#f5eed9')
      root.style.setProperty('--reading-toolbar-border', '#e8dcc0')
      root.style.setProperty('--reading-toolbar-text', '#5c4b37')
      root.style.setProperty('--reading-toolbar-icon', '#8b7355')
    } else if (theme.id === 'green') {
      // 绿色主题
      root.style.setProperty('--reading-hover-bg', 'rgba(22, 101, 52, 0.05)')
      root.style.setProperty('--reading-btn-bg', 'rgba(22, 101, 52, 0.08)')
      root.style.setProperty('--reading-btn-hover-bg', 'rgba(22, 101, 52, 0.12)')
      root.style.setProperty('--reading-comment-bg', 'rgba(16, 185, 129, 0.15)')
      root.style.setProperty('--reading-comment-btn-bg', 'rgba(16, 185, 129, 0.25)')
      root.style.setProperty('--reading-card-bg', 'rgba(236, 253, 245, 0.95)') // 表情选择器等卡片背景
      root.style.setProperty('--reading-border-color', '#d1fae5') // 分隔线颜色
      root.style.setProperty('--reading-active-icon', '#10b981') // 激活状态图标颜色
      root.style.setProperty('--reading-back-to-top-bg', 'linear-gradient(135deg, #10b981, #059669)') // 回到顶部按钮背景
      root.style.setProperty('--reading-back-to-top-shadow', 'rgba(16, 185, 129, 0.3)') // 回到顶部按钮阴影
      root.style.setProperty('--reading-back-to-top-shadow-hover', 'rgba(16, 185, 129, 0.4)') // 回到顶部按钮悬停阴影
      root.style.setProperty('--reading-tip-bg', 'linear-gradient(135deg, #10b981, #059669)') // 功能提示背景
      root.style.setProperty('--reading-tip-shadow', 'rgba(16, 185, 129, 0.2)') // 功能提示阴影
      // 底部工具栏
      root.style.setProperty('--reading-toolbar-bg', '#ecfdf5')
      root.style.setProperty('--reading-toolbar-border', '#d1fae5')
      root.style.setProperty('--reading-toolbar-text', '#166534')
      root.style.setProperty('--reading-toolbar-icon', '#059669')
    } else {
      // 默认主题
      root.style.setProperty('--reading-hover-bg', 'rgba(0, 0, 0, 0.03)')
      root.style.setProperty('--reading-btn-bg', 'rgba(0, 0, 0, 0.05)')
      root.style.setProperty('--reading-btn-hover-bg', 'rgba(0, 0, 0, 0.08)')
      root.style.setProperty('--reading-comment-bg', 'rgba(59, 130, 246, 0.05)')
      root.style.setProperty('--reading-comment-btn-bg', 'rgba(59, 130, 246, 0.15)')
      root.style.setProperty('--reading-card-bg', 'rgba(248, 249, 250, 0.95)') // 表情选择器等卡片背景
      root.style.setProperty('--reading-border-color', '#e5e7eb') // 分隔线颜色
      root.style.setProperty('--reading-active-icon', '#3b82f6') // 激活状态图标颜色
      root.style.setProperty('--reading-back-to-top-bg', 'linear-gradient(135deg, #3b82f6, #2563eb)') // 回到顶部按钮背景
      root.style.setProperty('--reading-back-to-top-shadow', 'rgba(59, 130, 246, 0.3)') // 回到顶部按钮阴影
      root.style.setProperty('--reading-back-to-top-shadow-hover', 'rgba(59, 130, 246, 0.4)') // 回到顶部按钮悬停阴影
      root.style.setProperty('--reading-tip-bg', 'linear-gradient(135deg, #3b82f6, #1d4ed8)') // 功能提示背景
      root.style.setProperty('--reading-tip-shadow', 'rgba(59, 130, 246, 0.2)') // 功能提示阴影
      // 底部工具栏
      root.style.setProperty('--reading-toolbar-bg', '#ffffff')
      root.style.setProperty('--reading-toolbar-border', '#e5e7eb')
      root.style.setProperty('--reading-toolbar-text', '#6b7280')
      root.style.setProperty('--reading-toolbar-icon', '#6b7280')
    }
    
    console.log('主题应用成功')
  } catch (error) {
    console.error('应用主题失败:', error)
  }
}

/**
 * 应用字体设置
 * @param {Number} fontSize - 字体大小
 * @param {Number} lineHeight - 行高
 */
export const applyFontSettings = (fontSize, lineHeight) => {
  console.log('应用字体设置 - 字体:', fontSize, 'px, 行高:', lineHeight)
  
  try {
    const root = document.documentElement
    root.style.setProperty('--reading-font-size', fontSize + 'px')
    root.style.setProperty('--reading-line-height', lineHeight)
    
    console.log('字体设置应用成功')
  } catch (error) {
    console.error('应用字体设置失败:', error)
  }
}

/**
 * 应用字体家族设置
 */
export const applyFontFamily = () => {
  console.log('应用字体家族设置')
  
  try {
    // 从存储中读取字体设置
    const fontFamilyCSS = uni.getStorageSync('fontFamilyCSS')
    
    if (fontFamilyCSS) {
      console.log('应用字体:', fontFamilyCSS)
      const root = document.documentElement
      root.style.setProperty('--reading-font-family', fontFamilyCSS)
    } else {
      console.log('未设置自定义字体，使用默认字体')
    }
  } catch (error) {
    console.error('应用字体家族失败:', error)
  }
}

/**
 * 检查是否支持屏幕常亮功能
 * @returns {Boolean} 是否支持
 */
export const isKeepScreenOnSupported = () => {
  // #ifdef H5
  console.log('H5平台不支持屏幕常亮功能')
  return false
  // #endif
  
  // #ifdef APP-PLUS || MP-WEIXIN || MP-ALIPAY || MP-BAIDU || MP-TOUTIAO || MP-QQ
  return true
  // #endif
  
  // 默认返回false
  return false
}

/**
 * 应用屏幕常亮设置
 * @param {Boolean} keepScreenOn - 是否保持屏幕常亮
 * @returns {Boolean} 是否设置成功
 */
export const applyKeepScreenOn = (keepScreenOn) => {
  console.log('设置屏幕常亮:', keepScreenOn)
  
  // 检查是否支持
  if (!isKeepScreenOnSupported()) {
    console.warn('当前平台不支持屏幕常亮功能')
    return false
  }
  
  try {
    // 检查API是否存在
    if (typeof uni.setKeepScreenOn !== 'function') {
      console.warn('uni.setKeepScreenOn API不存在，当前平台可能不支持')
      return false
    }
    
    uni.setKeepScreenOn({
      keepScreenOn: keepScreenOn,
      success: () => {
        console.log('屏幕常亮设置成功:', keepScreenOn)
      },
      fail: (error) => {
        console.warn('屏幕常亮设置失败(平台不支持):', error.errMsg)
      }
    })
    return true
  } catch (error) {
    console.error('设置屏幕常亮异常:', error)
    return false
  }
}

/**
 * 检查是否支持屏幕方向设置
 * @returns {Boolean} 是否支持
 */
export const isScreenOrientationSupported = () => {
  // #ifdef H5
  console.log('H5平台不支持屏幕方向设置')
  return false
  // #endif
  
  // #ifdef APP-PLUS
  return true
  // #endif
  
  // 小程序一般不支持
  return false
}

/**
 * 应用屏幕方向设置
 * @param {Boolean} landscapeLocked - 是否锁定横屏
 * @returns {Boolean} 是否设置成功
 */
export const applyScreenOrientation = (landscapeLocked) => {
  console.log('设置屏幕方向:', landscapeLocked ? '横屏' : '自动')
  
  // 检查是否支持
  if (!isScreenOrientationSupported()) {
    console.warn('当前平台不支持屏幕方向设置')
    return false
  }
  
  try {
    // 检查API是否存在
    if (typeof uni.setScreenOrientation !== 'function') {
      console.warn('uni.setScreenOrientation API不存在，当前平台可能不支持')
      return false
    }
    
    uni.setScreenOrientation({
      orientation: landscapeLocked ? 'landscape' : 'auto',
      success: () => {
        console.log('屏幕方向设置成功')
      },
      fail: (error) => {
        console.warn('屏幕方向设置失败(平台不支持):', error.errMsg)
      }
    })
    return true
  } catch (error) {
    console.error('设置屏幕方向异常:', error)
    return false
  }
}

/**
 * 重置为默认设置
 * @returns {Object} 默认设置对象
 */
export const resetToDefault = () => {
  console.log('重置为默认设置')
  saveSettings(defaultSettings)
  return { ...defaultSettings }
}

export default {
  themes,
  defaultSettings,
  saveSettings,
  loadSettings,
  getThemeById,
  applySettings,
  applyTheme,
  applyFontSettings,
  applyFontFamily,
  applyKeepScreenOn,
  applyScreenOrientation,
  isKeepScreenOnSupported,
  isScreenOrientationSupported,
  resetToDefault
}

