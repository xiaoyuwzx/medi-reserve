# MediReserve 前端核心功能模块说明

## 1. 患者端功能模块

### 1.1 认证模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 登录 | `/patient/login` | 手机号 + 密码登录，登录成功后跳转首页 |
| 注册 | `/patient/register` | 姓名、手机号、密码、身份证、性别注册 |

### 1.2 个人信息模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 个人信息 | `/patient/profile` | 查看/编辑姓名、手机号、身份证、性别 |
| 修改密码 | `/patient/password` | 验证旧密码后修改为新密码 |

### 1.3 号源展示模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 首页 | `/patient` | 热门医生排行 + 快捷入口（预约挂号/我的预约/我的评价） |
| 医生列表 | `/patient/doctors` | 科室筛选、关键词搜索（防抖500ms）、分页 |
| 医生详情 | `/patient/doctor/:id` | 医生信息 + 排班日历（7天）+ 评价列表 |

### 1.4 挂号支付模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 挂号确认 | `/patient/appointment/:scheduleId` | 排班详情确认，创建预约（待支付状态） |
| 支付确认 | `/patient/payment` | 确认支付（用户主动确认） |
| 支付结果 | `/patient/payment/result` | 支付成功/失败展示 |

### 1.5 预约评价模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 我的预约 | `/patient/appointments` | 状态筛选（全部/待支付/已支付/已就诊/已取消/已过期），去支付/去评价/进入问诊室 |
| 评价创建 | `/patient/evaluation/create/:id` | 评分（1-5星）+ 内容（最多500字）+ 匿名选项 |
| 我的评价 | `/patient/my-evaluations` | 评价列表展示，支持删除（软删除） |

### 1.6 问诊室模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 问诊室 | `/consultation/:appointmentId` | STOMP 实时聊天、历史消息、离线消息补发、结束问诊 |

---

## 2. 医生端功能模块

### 2.1 认证模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 登录 | `/doctor/login` | 手机号 + 密码登录，登录时校验审核状态 |
| 注册 | `/doctor/register` | 姓名、手机号、密码、科室、职称、擅长、简介等 |

### 2.2 个人信息模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 个人信息 | `/doctor/profile` | 基本信息 + 证件查看（当前生效/待审核）+ 证件上传（OSS 直传） |
| 修改密码 | `/doctor/password` | 验证旧密码后修改 |

### 2.3 首页模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 首页 | `/doctor` | 快捷入口（排班管理/问诊患者/数据统计/患者评价）+ 统计卡片 + 今日概览 |

### 2.4 排班管理模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 排班列表 | `/doctor/schedules` | 日期范围筛选、状态筛选、排班卡片展示 |
| 新增排班 | `/doctor/schedules/create` | 日期 + 时段 + 号源数 + 智能推荐号源 |

### 2.5 问诊患者模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 问诊患者 | `/doctor/patients` | 日期筛选、患者列表、进入问诊室 |

### 2.6 数据统计模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 数据统计 | `/doctor/statistics` | 总览卡片 + 接诊趋势图 + 评分趋势图 + 好评率趋势图 + 评价列表 |
| 评价列表 | `/doctor/evaluations` | 全部评价分页展示 |

---

## 3. 管理端功能模块

### 3.1 认证模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 登录 | `/admin/login` | 隐藏入口，用户名 + 密码登录 |
| 修改密码 | `/admin/password` | 验证旧密码后修改 |

### 3.2 首页布局

- 左侧菜单导航（12 个功能入口）
- 顶部用户信息 + 退出登录

### 3.3 审核管理模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 医生审核 | `/admin/audit/doctors` | 待审核医生列表（分页） |
| 审核详情 | `/admin/audit/doctors/:id` | 完整注册信息 + 证件图片 + 通过/驳回 |
| 证件审核 | `/admin/audit/cert` | 待审核证件列表 |
| 证件审核详情 | `/admin/audit/cert/:id` | 新旧证件对比 + 通过/驳回 |

### 3.4 账号管理模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 管理员管理 | `/admin/admins` | 列表展示、新增管理员（仅超级管理员）、启用/禁用 |
| 医生管理 | `/admin/doctors` | 已审核医生列表、关键词/科室/状态筛选、启用/禁用 |
| 患者管理 | `/admin/patients` | 患者列表、关键词/状态筛选、启用/禁用 |

### 3.5 数据统计模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 数据看板 | `/admin` | 总览卡片（8 项）+ 趋势图（天数切换）+ 科室排行表格 + 科室饼图 + 医生排行（挂号量/评分排序）+ 状态分布饼图 + 60秒自动刷新 |

### 3.6 操作日志模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 操作日志 | `/admin/logs` | 管理员/模块/结果/日期范围筛选 + 列表 + 详情 + 删除 |
| 日志详情 | `/admin/logs/:id` | 完整日志信息展示 |

### 3.7 权限管理模块

| 页面 | 路由 | 功能说明 |
| :--- | :--- | :--- |
| 权限树 | `/admin/permissions/tree` | 树形展示所有权限（菜单/按钮/接口） |
| 角色权限 | `/admin/permissions/roles` | 角色列表切换 + 权限树勾选分配 |

---

## 4. Pinia 状态管理

### 4.1 userStore（用户状态）

```typescript
// state
{
  token: string,
  userId: number | null,
  username: string,        // 登录账号（手机号）
  name: string,            // 真实姓名
  phone: string,           // 手机号
  idCard: string,          // 身份证号
  gender: number,          // 0-未知，1-男，2-女
  role: string             // PATIENT | DOCTOR | ADMIN | SUPER_ADMIN
}

// actions
setToken(token: string)
clearToken()
setUserInfo(userId, username, role, name, phone, idCard, gender)
updateProfile(name, phone, idCard, gender)
```

### 4.2 dictStore（字典状态）

```typescript
// state
{
  departments: DepartmentVO[],  // 科室列表
  titles: Title[],              // 职称列表
  isLoaded: boolean             // 是否已加载
}

// actions
loadDict()  // 并行加载科室和职称数据
```

## 5. WebSocket 在线问诊流程

### 5.1 连接建立

```
1. 用户点击"进入问诊室"
2. Room.vue 加载，获取 appointmentId
3. 调用 websocketApi.consultation.getRoomInfo(appointmentId) 获取房间信息
4. useWebSocket.connect(appointmentId) 建立 STOMP 连接
5. 连接成功后订阅：
   - /topic/room/{appointmentId}（房间消息）
   - /user/queue/messages（离线消息）
```

### 5.2 消息发送

```
1. 用户输入消息，点击发送
2. sendMessage(appointmentId, receiverId, content)
3. 发送到 /app/chat.send（固定路径）
4. 消息体：{ appointmentId, receiverId, content, msgType: 1 }
```

### 5.3 消息接收

```
1. 订阅 /topic/room/{appointmentId} 收到消息
2. 判断 isSelf = senderId === userId
3. 自己的消息显示在右侧（蓝色气泡）
4. 对方的消息显示在左侧（白色气泡）
5. 消息自动滚动到底部
```

### 5.4 离线消息

```
1. 发送消息时，后端判断接收者是否在线
2. 不在线 → 暂存到 Redis（ws:offline:{userId}）
3. 接收者上线时，自动推送 /user/queue/messages
4. 前端接收到后加入消息列表
```

### 5.5 结束问诊

```
1. 点击"结束问诊"按钮
2. ElMessageBox.confirm 二次确认
3. 调用 websocketApi.consultation.endConsultation(appointmentId)
4. 断开 WebSocket 连接
5. 返回上一页
```

## 6. 路由守卫

### 6.1 守卫逻辑

```typescript
// 管理端守卫
if (path.startsWith('/admin')) {
  if (!token) → /admin/login
  if (role !== 'ADMIN' && role !== 'SUPER_ADMIN') → 清除 token → /admin/login
}

// 医生端守卫
if (path.startsWith('/doctor')) {
  if (!token) → /doctor/login
  if (role !== 'DOCTOR') → 清除 token → /doctor/login
}

// 患者端守卫
if (path.startsWith('/patient')) {
  if (!token) → /patient/login
  if (role !== 'PATIENT') → 清除 token → /patient/login
}

// 问诊室守卫
if (path.startsWith('/consultation')) {
  if (!token) → /patient/login
  if (role !== 'PATIENT' && role !== 'DOCTOR') → /patient
}
```

### 6.2 角色与路径映射

| 角色 | 路径前缀 | 登录页 | 说明 |
| :--- | :--- | :--- | :--- |
| PATIENT | `/patient` | `/patient/login` | 患者端 |
| DOCTOR | `/doctor` | `/doctor/login` | 医生端 |
| ADMIN / SUPER_ADMIN | `/admin` | `/admin/login` | 管理端 |
| PATIENT / DOCTOR | `/consultation` | — | 问诊室（需登录） |