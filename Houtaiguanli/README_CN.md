# Houtaiguanli - 后台管理系统

## 项目简介

这是一个基于Vue 3 + Element Plus的现代化后台管理系统，用于管理阅桥亲子阅读APP平台的用户、内容等资源。

## 功能特性

- ✨ 用户管理（增、删、改、查、状态管理）
- 🔍 高级搜索与分页
- 📊 数据统计与分析
- 🎨 现代化UI设计
- ⚡ 实时API响应

## 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0
- Spring Boot 后端服务运行在 `http://localhost:8888`

## 快速开始

### 1. 安装依赖

```bash
cd D:\社团组织\社团组织\大创-阅桥亲子阅读APP\Houtaiguanli
npm install
```

### 2. 配置API连接

创建 `.env.local` 文件（如果不存在），配置API基础地址：

```bash
# .env.local
VITE_API_URL=http://localhost:8888
```

如果后端服务运行在其他地址，修改此配置即可。

### 3. 启动开发服务器

```bash
npm run dev
```

访问 `http://localhost:5173` 查看应用（端口可能不同，请查看控制台输出）

### 4. 构建生产版本

```bash
npm run build
```

## Spring Boot 后端连接说明

### 后端服务配置

后端Spring Boot应用默认配置：
- **服务地址**: `http://localhost:8888`
- **数据库**: MySQL (localhost:3306)
- **Redis**: 127.0.0.1:6379
- **用户名**: root / 密码: 123456

### 用户管理API端点

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/api/user/page` | 分页查询用户列表 |
| POST | `/api/user` | 创建新用户 |
| GET | `/api/user/{id}` | 获取用户详情 |
| PUT | `/api/user` | 更新用户信息 |
| DELETE | `/api/user/{id}` | 删除用户 |
| PATCH | `/api/user/{id}/status` | 更新用户状态 |
| DELETE | `/api/user/batch` | 批量删除用户 |
| PATCH | `/api/user/batch/status` | 批量更新用户状态 |

### API响应格式

后端返回的响应格式遵循统一的Result类：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "user1",
        "nickname": "用户1",
        "phone": "13800138000",
        "email": "user1@example.com",
        "role": 1,
        "status": 1,
        "avatar": "http://...",
        "createdTime": "2024-10-23 10:00:00"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

## 项目结构

```
src/
├── api/
│   ├── user.js              # 用户管理API模块
│   └── questionnaire.js     # 问卷API模块
├── components/              # 可复用组件
├── layout/                  # 布局组件
├── router/                  # 路由配置
├── utils/
│   ├── request.js          # API请求封装
│   └── ...
├── views/
│   ├── user/               # 用户管理页面
│   ├── dashboard/          # 仪表盘
│   └── ...
├── assets/                 # 静态资源
└── App.vue                 # 根组件
```

## 功能说明

### 用户管理页面

路径: `/views/user/index.vue`

**主要功能**:
- 📋 用户列表展示（分页）
- 🔍 按用户名、角色、状态搜索筛选
- ➕ 新增用户
- ✏️ 编辑用户信息
- 🚫 删除用户
- 🔒 启用/禁用用户
- 👥 批量操作（可扩展）

**用户角色**:
- 1: 家长
- 2: 孩子

**用户状态**:
- 1: 正常
- 0: 禁用

## 常见问题解决

### 1. 无法连接到后端服务

**问题**: 前端报错 "无法连接到服务器"

**解决方案**:
- 确认Spring Boot服务已启动
- 检查后端服务地址配置是否正确
- 查看浏览器开发工具Network标签，确认请求是否正确

### 2. CORS跨域错误

**问题**: 浏览器报错 "Access to XMLHttpRequest has been blocked by CORS policy"

**解决方案**:
- 后端已配置@CrossOrigin注解
- 检查后端是否正确启动
- 清除浏览器缓存，重新加载页面

### 3. API返回401未授权

**问题**: 后端返回401错误

**解决方案**:
- 某些API需要JWT token认证
- 确保在localStorage中存储了有效的token
- 检查token是否过期

## 开发指南

### 添加新的API接口

1. 在 `src/api/` 下创建新的接口文件
2. 使用 `request` 实例发起请求
3. 导出API函数供页面使用

### 修改请求拦截器

在 `src/utils/request.js` 中修改请求/响应拦截器逻辑

### 调整后端连接地址

修改 `.env.local` 文件中的 `VITE_API_URL` 变量

## 技术栈

- Vue 3
- Vite
- Element Plus
- Axios
- Pinia
- Vue Router
- Sass

## 许可证

© 2024 阅桥亲子阅读APP平台

## 支持

如有问题，请查看后端项目文档或联系开发团队。









