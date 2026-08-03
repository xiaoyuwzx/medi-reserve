# MediReserve 前端架构设计文档

## 1. 技术栈概述

| 类别 | 技术 | 版本 |
| :--- | :--- | :---: |
| 核心框架 | Vue 3 | — |
| 构建工具 | Vite | 8.x |
| 语言 | TypeScript | — |
| 状态管理 | Pinia | — |
| UI 组件库 | Element Plus | — |
| 路由 | Vue Router | — |
| HTTP 客户端 | Axios | — |
| 图表 | ECharts | 6.1.0 |
| WebSocket | @stomp/stompjs + sockjs-client | — |
| 文件上传 | ali-oss | — |

## 2. 项目结构

```
medi-reserve-frontend/
├── src/
│   ├── api/                          # API 请求层
│   │   ├── request.ts                # 请求拦截器配置
│   │   ├── instances.ts              # 各模块 Axios 实例
│   │   ├── patient/                  # 患者端 API
│   │   │   ├── patientApi.ts         # Swagger 生成
│   │   │   └── index.ts              # 导出 patientApi
│   │   ├── doctor/                   # 医生端 API
│   │   │   ├── doctorApi.ts          # Swagger 生成
│   │   │   └── index.ts              # 导出 doctorApi
│   │   ├── admin/                    # 管理端 API
│   │   │   ├── adminApi.ts           # Swagger 生成
│   │   │   └── index.ts              # 导出 adminApi
│   │   ├── websocket/                # WebSocket HTTP API
│   │   │   ├── websocketApi.ts       # Swagger 生成
│   │   │   └── index.ts              # 导出 websocketApi
│   │   └── types/                    # 类型定义
│   ├── components/                   # 公共组件
│   │   ├── patient/                  # 患者端专用组件
│   │   │   ├── DoctorCard.vue
│   │   │   ├── ScheduleCalendar.vue
│   │   │   └── HotDoctors.vue
│   │   ├── doctor/                   # 医生端专用组件
│   │   │   └── ScheduleCard.vue
│   │   └── admin/                    # 管理端专用组件
│   │       └── AuditDialog.vue
│   ├── views/                        # 页面组件
│   │   ├── patient/                  # 患者端页面（13 个页面）
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   ├── Home.vue
│   │   │   ├── DoctorList.vue
│   │   │   ├── DoctorDetail.vue
│   │   │   ├── AppointmentConfirm.vue
│   │   │   ├── PaymentPage.vue
│   │   │   ├── PaymentResult.vue
│   │   │   ├── MyAppointments.vue
│   │   │   ├── EvaluationCreate.vue
│   │   │   ├── MyEvaluations.vue
│   │   │   ├── Profile.vue
│   │   │   └── Password.vue
│   │   ├── doctor/                   # 医生端页面（9 个页面）
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   ├── Home.vue
│   │   │   ├── Profile.vue
│   │   │   ├── Password.vue
│   │   │   ├── ScheduleList.vue
│   │   │   ├── ScheduleCreate.vue
│   │   │   ├── PatientList.vue
│   │   │   ├── Statistics.vue
│   │   │   └── Evaluations.vue
│   │   ├── admin/                    # 管理端页面（12 个页面）
│   │   │   ├── Login.vue
│   │   │   ├── Home.vue
│   │   │   ├── Dashboard.vue
│   │   │   ├── Password.vue
│   │   │   ├── DoctorAuditList.vue
│   │   │   ├── DoctorAuditDetail.vue
│   │   │   ├── CertAuditList.vue
│   │   │   ├── CertAuditDetail.vue
│   │   │   ├── AdminList.vue
│   │   │   ├── DoctorManageList.vue
│   │   │   ├── PatientManageList.vue
│   │   │   ├── LogList.vue
│   │   │   ├── LogDetail.vue
│   │   │   ├── PermissionTree.vue
│   │   │   └── RolePermission.vue
│   │   └── consultation/             # 问诊室页面
│   │       └── Room.vue
│   ├── layouts/                      # 布局组件
│   │   └── AdminLayout.vue            # 管理端左侧菜单布局
│   ├── router/                       # 路由配置
│   │   └── index.ts                  # 路由定义 + 路由守卫
│   ├── stores/                       # Pinia 状态管理
│   │   ├── user.ts                   # 用户状态（token/角色/个人信息）
│   │   └── dict.ts                   # 字典状态（科室/职称）
│   ├── composables/                  # 组合式函数
│   │   ├── useWebSocket.ts           # WebSocket 连接管理
│   │   └── useRequest.ts             # 请求封装
│   ├── App.vue                       # 根组件
│   └── main.ts                       # 应用入口
├── docs/                             # 文档目录
├── package.json
├── vite.config.ts
├── tsconfig.json
└── index.html
```

## 3. 模块职责划分

| 模块 | 职责 | 后端端口 |
| :--- | :--- | :---: |
| 患者端 | 注册登录、号源展示、挂号支付、评价、问诊 | 8081 |
| 医生端 | 排班管理、问诊患者、数据统计、证件上传 | 8082 |
| 管理端 | 医生审核、证件审核、管理员管理、统计看板、操作日志、权限管理 | 8083 |
| WebSocket | 在线问诊实时通信 | 8084 |

## 4. 多端隔离方案

- 使用 `sessionStorage` 存储登录态（token、userId、userRole 等），不同标签页独立
- 路由守卫校验 `role` 与当前访问端匹配
- 各端 API 实例独立：
  - `patientInstance` (baseURL: `/api-patient`)
  - `doctorInstance` (baseURL: `/api-doctor`)
  - `adminInstance` (baseURL: `/api-admin`)
  - `websocketInstance` (baseURL: `/api-websocket`)

## 5. 路由总览

| 模块 | 路由数量 | 路径前缀 |
| :--- | :---: | :--- |
| 患者端 | 13 条 | `/patient/*` |
| 医生端 | 10 条 | `/doctor/*` |
| 管理端 | 12 条（嵌套） | `/admin/*` |
| 问诊室 | 1 条 | `/consultation/*` |