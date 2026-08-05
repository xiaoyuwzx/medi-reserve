# MediReserve 前端 API 接口对接文档

## 1. 请求封装机制

### 拦截器架构

`src/api/request.ts` 提供了统一的拦截器工厂 `applyInterceptors(instance)`：

**请求拦截器**：
- 自动从 `userStore.token` 获取 JWT Token
- 添加到请求头：`Authorization: Bearer {token}`
- 无 Token 时不添加（登录/注册接口不需要）

**响应拦截器**：
- 后端统一返回格式：`{ code, msg, data }`
- `code === 1` → 直接返回 `data` 字段（业务数据）
- `code !== 1` → 抛出 `Error`（`msg` 作为错误信息）
- `401` → 自动清除 token 并跳转登录页
- 其他 HTTP 错误 → 抛出错误信息

### 响应解包示例

```typescript
// 后端返回
{ "code": 1, "msg": "成功", "data": { "token": "xxx", "id": 1 } }

// 拦截器处理后
{ "token": "xxx", "id": 1 }  // 直接返回 data
```

## 2. API 实例配置

| 实例名 | baseURL | 代理目标 | 端口 | 用途 |
| :--- | :--- | :--- | :---: | :--- |
| `patientInstance` | `/api-patient` | `http://localhost:8081` | 8081 | 患者端 API |
| `doctorInstance` | `/api-doctor` | `http://localhost:8082` | 8082 | 医生端 API |
| `adminInstance` | `/api-admin` | `http://localhost:8083` | 8083 | 管理端 API |
| `websocketInstance` | `/api-websocket` | `http://localhost:8084` | 8084 | WebSocket HTTP API |

所有实例统一配置：

- `timeout: 10000`
- `Content-Type: application/json`
- 统一调用 `applyInterceptors()` 注入拦截器

## 3. 患者端 API

### 认证管理

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `patientApi.patient.login` | `POST /patient/login` | 手机号+密码登录 |
| `patientApi.patient.register` | `POST /patient/register` | 患者注册 |
| `patientApi.patient.updateProfile` | `PUT /patient/profile` | 修改个人信息 |
| `patientApi.patient.updatePassword` | `PUT /patient/password` | 修改密码 |

**登录示例**：

```typescript
const res = await patientApi.patient.login({
  username: '13800000000',
  password: '123456'
})
// res: { token, id, name, phone }
userStore.setToken(res.token)
userStore.setUserInfo(res.id, res.phone, 'PATIENT', res.name, res.phone, '', 0)
```

### 号源展示

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `patientApi.patient.getAllDepartments` | `GET /patient/departments` | 科室列表（缓存1h） |
| `patientApi.patient.getDoctorList` | `GET /patient/doctors` | 医生列表（分页+筛选） |
| `patientApi.patient.getScheduleCalendar` | `GET /patient/doctors/{id}/schedules` | 排班日历（7天） |
| `patientApi.patient.getDoctorEvaluations` | `GET /patient/doctors/{id}/evaluations` | 医生评价列表 |
| `patientApi.patient.getHotDoctors` | `GET /patient/doctors/hot` | 热门医生排行 |

**医生列表示例**：

```typescript
const res = await patientApi.patient.getDoctorList({
  department: '内科',
  keyword: '张',
  page: 1,
  size: 10
})
// res: PageInfo<DoctorListVO>
```

### 预约挂号

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `patientApi.patient.getScheduleDetail` | `GET /patient/schedules/{id}` | 排班详情 |
| `patientApi.patient.createAppointment` | `POST /patient/appointments` | 创建预约 |
| `patientApi.patient.payAppointment` | `POST /patient/appointments/{id}/pay` | 模拟支付 |
| `patientApi.patient.getMyAppointments` | `GET /patient/appointments` | 我的预约列表 |

**创建预约示例**：

```typescript
const res = await patientApi.patient.createAppointment({
  scheduleId: 37
})
// res: { appointmentId, appointmentNo, status, statusText, payDeadline }
```

### 评价管理

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `patientApi.patient.createEvaluation` | `POST /patient/evaluations` | 创建评价 |
| `patientApi.patient.getMyEvaluations` | `GET /patient/my-evaluations` | 我的评价列表 |
| `patientApi.patient.deleteEvaluation` | `DELETE /patient/evaluations/{id}` | 删除评价 |

## 4. 医生端 API

### 认证管理

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `doctorApi.doctor.login` | `POST /doctor/login` | 手机号+密码登录 |
| `doctorApi.doctor.register` | `POST /doctor/register` | 医生注册 |
| `doctorApi.doctor.updateProfile` | `PUT /doctor/profile` | 修改个人信息 |
| `doctorApi.doctor.updatePassword` | `PUT /doctor/password` | 修改密码 |
| `doctorApi.doctor.getAuditStatus` | `GET /doctor/profile/audit-status` | 查询证件审核状态 |

### 排班管理

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `doctorApi.doctor.listSchedules` | `GET /doctor/schedules` | 排班列表（扁平参数） |
| `doctorApi.doctor.createSchedule` | `POST /doctor/schedules` | 新增排班 |
| `doctorApi.doctor.updateScheduleStatus` | `PATCH /doctor/schedules/{id}/status` | 停诊/恢复 |
| `doctorApi.doctor.deleteSchedule` | `DELETE /doctor/schedules/{id}` | 删除排班 |
| `doctorApi.doctor.getRecommendedMaxCount` | `GET /doctor/schedules/recommend` | 智能推荐号源 |

**排班列表示例（注意：扁平参数，非嵌套对象）**：

```typescript
const res = await doctorApi.doctor.listSchedules({
  startDate: '2026-08-01',
  endDate: '2026-08-07',
  status: 1
})
```

### 问诊患者

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `doctorApi.doctor.getDoctorAppointments` | `GET /doctor/appointments` | 查询预约患者 |

### 数据统计

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `doctorApi.doctor.getOverview` | `GET /doctor/statistics/overview` | 总览统计 |
| `doctorApi.doctor.getTrend` | `GET /doctor/statistics/trend` | 每日接诊趋势 |
| `doctorApi.doctor.getEvaluations` | `GET /doctor/statistics/evaluations` | 评价列表 |

### 文件上传

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `doctorApi.doctor.getStsToken` | `GET /doctor/oss/sts` | 获取 STS 临时凭证 |

## 5. 管理端 API

### 认证管理

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `adminApi.admin.login` | `POST /admin/login` | 管理员登录 |
| `adminApi.admin.updatePassword` | `PUT /admin/password` | 修改密码 |

### 医生审核

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `adminApi.admin.listPending` | `GET /admin/doctors/pending` | 待审核医生列表 |
| `adminApi.admin.getAuditDetail` | `GET /admin/doctors/{id}/audit-detail` | 审核详情 |
| `adminApi.admin.approve` | `PATCH /admin/doctors/{id}/approve` | 审核通过 |
| `adminApi.admin.reject` | `PATCH /admin/doctors/{id}/reject` | 审核驳回 |

### 证件审核

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `adminApi.admin.listCertPending` | `GET /admin/cert-pending` | 待审核证件列表 |
| `adminApi.admin.getCertPendingDetail` | `GET /admin/{doctorId}/cert-pending-detail` | 证件审核详情 |
| `adminApi.admin.auditCertificate` | `PATCH /admin/{doctorId}/cert-audit` | 审核证件 |

### 管理员管理

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `adminApi.admin.listAdmins` | `GET /admin/list` | 管理员列表 |
| `adminApi.admin.register` | `POST /admin/register` | 新增管理员 |
| `adminApi.admin.updateStatus` | `PATCH /admin/{id}/status` | 启用/禁用 |

### 数据统计

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `adminApi.admin.getOverview` | `GET /admin/dashboard/overview` | 总览统计 |
| `adminApi.admin.getTrend` | `GET /admin/dashboard/trend` | 趋势数据 |
| `adminApi.admin.getDepartmentRanking` | `GET /admin/dashboard/department-ranking` | 科室排行 |
| `adminApi.admin.getDoctorRanking` | `GET /admin/dashboard/doctor-ranking` | 医生排行 |
| `adminApi.admin.getStatusDistribution` | `GET /admin/dashboard/status-distribution` | 状态分布 |

### 操作日志

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `adminApi.admin.list` | `GET /admin/operation-logs` | 日志列表 |
| `adminApi.admin.detail` | `GET /admin/operation-logs/{id}` | 日志详情 |
| `adminApi.admin.delete` | `DELETE /admin/operation-logs/{id}` | 删除日志 |

### 权限管理

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `adminApi.admin.getPermissionTree` | `GET /admin/permissions/tree` | 权限树 |
| `adminApi.admin.getAllRolesWithPermissions` | `GET /admin/permissions/roles` | 角色权限 |
| `adminApi.admin.updateRolePermissions` | `PUT /admin/permissions/roles/{roleId}/permissions` | 更新角色权限 |

### 账号管理（扩展接口）

以下接口未在 `adminApi.ts` 中自动生成，需使用 `adminApi.instance.request` 调用：

| 路径 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/admin/doctors` | GET | 医生账号管理列表 |
| `/admin/doctors/{id}` | GET | 医生详情 |
| `/admin/doctors/{id}/status` | PATCH | 修改医生状态 |
| `/admin/patients` | GET | 患者账号管理列表 |
| `/admin/patients/{id}` | GET | 患者详情 |
| `/admin/patients/{id}/status` | PATCH | 修改患者状态 |

**调用示例**：

```typescript
import { adminApi } from '@/api/admin'

const res = await adminApi.instance.request({
  url: '/admin/doctors',
  method: 'GET',
  params: { keyword, departmentId, status, page, size }
})
```

## 6. WebSocket HTTP API

| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| `websocketApi.consultation.getRoomInfo` | `GET /consultation/room/{appointmentId}` | 房间信息 |
| `websocketApi.consultation.getHistory` | `GET /consultation/history/{appointmentId}` | 历史消息 |
| `websocketApi.consultation.endConsultation` | `POST /consultation/end/{appointmentId}` | 结束问诊 |

## 7. 类型定义索引

| 类型 | 模块 | 说明 |
| :--- | :--- | :--- |
| `LoginDTO` | 所有模块 | 登录请求 |
| `DoctorListVO` | patient | 医生列表项 |
| `ScheduleCalendarVO` | patient | 排班日历项 |
| `AppointmentListVO` | patient/doctor | 预约列表项 |
| `ScheduleDetailVO` | patient | 排班详情 |
| `DoctorStatisticsOverviewVO` | doctor | 医生统计总览 |
| `DashboardOverviewVO` | admin | 管理端总览 |
| `ChatMessageVO` | websocket | 聊天消息 |
| `ConsultationRoomVO` | websocket | 问诊室信息 |
| `OssStsVO` | doctor | OSS 临时凭证 |

## 8. 注意事项

### 参数格式

- `listSchedules` 使用扁平参数：`{ startDate, endDate, status }`
- `getDoctorList` 在患者端页面中已调整为扁平参数：`{ department, keyword, page, size }`

### 分页参数

- `page`：从 1 开始
- `size`：默认 10，最大 100

### 日期格式

- 统一使用 `YYYY-MM-DD`
- 使用 `dayjs().format('YYYY-MM-DD')` 生成

### 响应解包

- 所有 API 调用返回的是已解包的 `data` 字段
- 无需再写 `res.data.data`