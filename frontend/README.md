# 亲子阅读应用前端

这是一个基于 uni-app 开发的亲子阅读应用前端项目。使用 Vue 3 + Composition API 开发。

## 项目结构

```
frontend/
├── pages/              # 页面文件夹
│   ├── home/          # 首页
│   ├── login/         # 登录页
│   ├── register/      # 注册页
│   ├── read/          # 阅读页
│   └── profile/       # 用户中心页
├── static/            # 静态资源
├── App.vue            # 应用入口组件
├── main.js            # 应用入口文件
├── pages.json         # 页面路由配置
└── package.json       # 项目配置文件
```

## 开发环境要求

- Node.js 14.0+
- HBuilderX 3.0+

## 安装和运行

1. 安装依赖：
```bash
npm install
```

2. 开发运行：
使用 HBuilderX 打开项目，点击"运行"按钮选择需要运行的平台。

## 页面说明

- 登录页（/pages/login/login）：用户登录界面
- 注册页（/pages/register/register）：新用户注册界面
- 首页（/pages/home/home）：展示推荐书籍和阅读内容
- 阅读页（/pages/read/read）：书籍阅读界面
- 用户中心（/pages/profile/profile）：用户信息和设置

## 技术栈

- Vue 3
- uni-app
- Composition API 