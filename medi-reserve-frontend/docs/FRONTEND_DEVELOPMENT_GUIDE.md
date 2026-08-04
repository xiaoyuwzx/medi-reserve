# MediReserve 前端开发指南

## 1. 环境要求

| 组件 | 版本要求 | 说明 |
| :--- | :---: | :--- |
| Node.js | 18+ | 推荐使用 LTS 版本 |
| npm | 9+ | 或使用 pnpm / yarn |
| 后端服务 | — | 需同时启动 4 个后端服务（8081-8084） |

## 2. 快速启动

```bash
# 1. 进入前端项目目录
cd medi-reserve-frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 4. 访问
http://localhost:5173
```

### 各端访问地址

| 端 | 地址 | 说明 |
| :--- | :--- | :--- |
| 患者端 | `http://localhost:5173/patient/login` | 注册/登录入口 |
| 医生端 | `http://localhost:5173/doctor/login` | 注册/登录入口 |
| 管理端 | `http://localhost:5173/admin/login` | 隐藏入口（直接访问） |
| 问诊室 | `http://localhost:5173/consultation/{appointmentId}` | 需登录且角色为患者/医生 |

### 默认测试账号

| 端 | 用户名 | 密码 | 角色 |
| :--- | :--- | :--- | :--- |
| 患者端 | 19800000001 | 123456 | PATIENT |
| 医生端 | 13800001111 | 123456 | DOCTOR |
| 管理端 | admin | 123456 | SUPER_ADMIN |

## 3. 环境变量配置

| 变量 | 说明 | 默认值 |
| :--- | :--- | :--- |
| VITE_API_BASE | 后端基础地址 | http://localhost:8081 |
| VITE_WEBSOCKET_BASE | WebSocket 地址 | ws://localhost:8084 |

## 4. Vite 代理配置

在 `vite.config.ts` 中已配置以下代理规则：

| 前缀 | 目标 | 说明 |
| :--- | :--- | :--- |
| `/api-patient` | `http://localhost:8081` | 患者端 API 代理 |
| `/api-doctor` | `http://localhost:8082` | 医生端 API 代理 |
| `/api-admin` | `http://localhost:8083` | 管理端 API 代理 |
| `/api-websocket` | `http://localhost:8084` | WebSocket 代理（含 `ws: true`） |

## 5. 常用 npm 命令

| 命令 | 说明 |
| :--- | :--- |
| `npm run dev` | 启动开发服务器 |
| `npm run build` | 生产构建 |
| `npm run preview` | 预览生产构建 |
| `npm run type-check` | TypeScript 类型检查 |
| `npm run gen-api` | 重新生成 API 代码（从 OpenAPI 文档） |

## 6. 代码风格规范

### 组件命名
- 组件文件：PascalCase（如 `DoctorCard.vue`、`ScheduleList.vue`）
- 页面组件：PascalCase（如 `MyAppointments.vue`）

### 变量命名
- 普通变量：camelCase（如 `userStore`、`queryParams`）
- 常量：UPPER_SNAKE_CASE（如 `REFRESH_INTERVAL`）
- 类型/接口：PascalCase（如 `ChatMessageVO`、`UserInfo`）

### Vue 组件结构

```vue
<script setup lang="ts">
// 1. 导入
// 2. 状态定义（ref / reactive）
// 3. 计算属性（computed）
// 4. 方法定义
// 5. 生命周期钩子
</script>

<template>
  <!-- 模板内容 -->
</template>

<style scoped>
/* 样式 */
</style>
```

### TypeScript 类型
- 优先使用 `interface` 定义对象类型
- 使用 `type` 定义联合类型或工具类型
- 从 API 模块导入类型：`import type { AppointmentListVO } from '@/api/patient/patientApi'`

## 7. Pinia Store 使用规范

```typescript
// 使用 store
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()

// 在模板中使用
<span>{{ userStore.name || userStore.username }}</span>

// 调用 action
userStore.setToken(token)
userStore.clearToken()
userStore.updateProfile(name, phone, idCard, gender)
```

## 8. API 调用规范

```typescript
// 患者端
import { patientApi } from '@/api/patient'
const res = await patientApi.patient.login({ username, password })

// 医生端
import { doctorApi } from '@/api/doctor'
const res = await doctorApi.doctor.listSchedules({ startDate, endDate })

// 管理端（未在 API 文件中生成的接口）
import { adminApi } from '@/api/admin'
const res = await adminApi.instance.request({
  url: '/admin/doctors',
  method: 'GET',
  params: { keyword, page, size }
})
```

## 9. 调试技巧

### 查看 API 请求详情
- 打开浏览器开发者工具（F12）→ Network 标签
- 查看请求/响应数据

### 查看 Pinia 状态
- 安装 Vue Devtools 插件
- 在 Devtools 中查看 Pinia 面板

### 查看 WebSocket 消息
- 打开开发者工具 → Network 标签 → WS 子标签
- 选择 WebSocket 连接查看消息帧

### 查看拦截器日志
- `src/api/request.ts` 中已配置日志输出
- 打开控制台即可看到请求/响应日志

## 10. 常见问题排查

### Q1: 登录后刷新页面跳转登录页
**原因**：`sessionStorage` 中的 `userRole` 不存在或已过期。
**解决**：检查 `stores/user.ts` 中 `role` 是否正确持久化，确认退出登录时清除了所有 `sessionStorage` 键。

### Q2: WebSocket 连接失败
**原因**：Vite 代理未正确配置 WebSocket 升级。
**解决**：检查 `vite.config.ts` 中 `/api-websocket` 代理是否包含 `ws: true`。

### Q3: 管理端菜单项不完整
**原因**：当前登录角色不是 `SUPER_ADMIN`。
**解决**：使用超级管理员账号（`admin`）登录，权限树和角色权限菜单仅超级管理员可见。

### Q4: 医生端排班筛选不生效
**原因**：API 参数格式错误。
**解决**：`listSchedules` 使用扁平参数（`{ startDate, endDate, status }`），而非嵌套对象。

### Q5: 图片上传失败 403
**原因**：OSS Bucket 未关闭阻止公共访问，或 STS 凭证权限不足。
**解决**：在 OSS 控制台关闭"阻止公共访问"，设置 Bucket 为公共读。

## 11. 项目构建与部署

### 开发环境构建

```bash
npm run build
# 构建产物在 dist/ 目录
```

### 生产环境部署

```bash
# 1. 构建
npm run build

# 2. 部署 dist/ 目录到 Web 服务器（Nginx / Apache）
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api-patient/ {
        proxy_pass http://localhost:8081/;
        proxy_set_header Host $host;
    }
    # 同理配置 /api-doctor、/api-admin、/api-websocket
}