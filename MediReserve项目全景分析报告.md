# MediReserve 项目全景分析报告

> 生成时间：2026-07-19 | 版本：1.0 | 基于源码深度扫描

---

## 一、项目总览

### 一句话定位
**MediReserve** 是一个基于 Spring Boot 3 + Vue 3 的**智慧医疗预约挂号平台**，采用"三端分离"架构，覆盖患者挂号、医生排班、管理员审核的完整业务闭环，并集成 WebSocket 在线问诊功能。

### 核心业务目标
1. **三端分离**：患者端（挂号问诊）、医生端（排班管理）、管理端（审核监管），各端独立部署、独立认证
2. **挂号闭环**：排班展示 → 预约下单 → 锁号扣减 → 模拟支付 → 超时回滚 → 就诊评价
3. **高并发处理**：Redisson 分布式锁 + 数据库乐观锁 + 多级缓存（Caffeine + Redis）+ 布隆过滤器防穿透 + 时间轮定时取消

### 技术栈清单

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 3.3.6 |
| **JDK** | Java | 17 |
| **ORM** | MyBatis (+ Spring Boot Starter) | 3.0.3 |
| **数据库** | MySQL | 8.x (mysql-connector-j) |
| **缓存** | Redis + Caffeine（多级缓存） | Caffeine 3.1.8 |
| **分布式锁** | Redisson | 3.27.2 |
| **时间轮** | Netty HashedWheelTimer | 4.1.115.Final |
| **JWT** | JJWT (io.jsonwebtoken) | 0.12.5 |
| **密码加密** | Spring Security Crypto (BCrypt) | 6.3.5 |
| **API 文档** | Knife4j (OpenAPI 3 Jakarta) | 4.5.0 |
| **工具库** | Hutool | 5.8.26 |
| **分页** | PageHelper | 2.1.0 |
| **布隆过滤器** | Google Guava | 33.2.0-jre |
| **OSS** | 阿里云 OSS SDK + STS | 3.17.4 / 3.1.1 |
| **前端框架** | Vue 3 + Vite | Vue 3.5.38 / Vite 8.0 |
| **UI 库** | Element Plus | 2.14.3 |
| **状态管理** | Pinia | 3.0.4 |
| **路由** | Vue Router | 5.1.0 |
| **HTTP 客户端** | Axios | 1.18.1 |
| **WebSocket** | @stomp/stompjs + SockJS | 7.3.0 / 1.6.1 |
| **JWT 解码** | jwt-decode | 4.0.0 |
| **容器化** | Docker + Docker Compose + Nginx | - |

---

## 二、后端架构分析

### 2.1 模块依赖关系

```
                    medi-reserve (父 POM)
                          │
              ┌───────────┼───────────┬───────────────┐
              │           │           │               │
         medi-common  medi-patient  medi-doctor  medi-admin  medi-websocket
         (公共模块)    (患者端:8081) (医生端:8082) (管理端:8083) (WebSocket:8084)
              │           │           │               │
              └───────────┴───────────┴───────────────┘
                    所有业务模块都依赖 medi-common
```

**依赖关系说明：**
- **medi-common**：纯公共模块（JAR），不独立启动——提供所有实体、DTO、通用 Mapper、配置、拦截器、异常处理、工具类
- **medi-patient / medi-doctor / medi-admin / medi-websocket**：四个独立的 Spring Boot 应用（各自有 `Application` 启动类和独立端口），均依赖 `medi-common`
- **medi-common** 包含了所有 Mapper 接口（`@Mapper`），业务模块通过 `@Autowired` 直接注入使用

### 2.2 medi-common 公共模块

#### 2.2.1 annotation 包 — 权限注解

| 文件 | 用途 |
|------|------|
| `RequireRole.java` | 方法级注解，标注允许的角色列表 `@RequireRole({"PATIENT", "DOCTOR"})`，由 `JwtTokenInterceptor` 解析并校验 |

#### 2.2.2 config 包 — 核心配置类

| 文件 | 用途 |
|------|------|
| **`RedisConfig.java`** | 配置 `RedisTemplate<String, Object>`，使用 `GenericJackson2JsonRedisSerializer` 序列化，注册 `JavaTimeModule` 支持 `LocalDateTime` |
| **`RedissonConfig.java`** | 配置 `RedissonClient` Bean（单节点模式），连接池 10，最小空闲 2，用于分布式锁（挂号扣号源、取消预约） |
| **`CacheConfig.java`** | 启用 `@EnableCaching`，配置 `RedisCacheManager`，各缓存区 TTL：`departments`(1h), `titles`(1h), `doctors`(5min), `schedules`(1min)，支持缓存空值防穿透 |
| **`MultiLevelCacheConfig.java`** | 多级缓存配置（Caffeine L1 + Redis L2），提供 `MultiLevelCacheService` |
| **`DefaultWebMvcConfig.java`** | 注册 `JwtTokenInterceptor`，配置 CORS 跨域，放行路径包括三端登录注册、`/patient/dict/**`、`/patient/departments`、`/patient/doctors`、静态资源/Swagger |
| **`JwtInterceptorProperties.java`** | 读取 `application.yml` 中 `jwt.interceptor.exclude-paths`，支持子模块按需覆盖放行路径 |
| **`Knife4jConfig.java`** | Knife4j 接口文档配置 |
| **`BloomFilterConfig.java`** | 初始化 Guava `BloomFilter`，启动时加载未来所有排班 ID，用于排班查询防缓存穿透 |
| **`ResourceConfig.java`** | 静态资源配置（如文件上传映射） |

#### 2.2.3 constant 包 — 常量定义

| 文件 | 内容 |
|------|------|
| `CacheKeyConstants` | 缓存 Key 构建工具：`buildDoctorsKey()`、`buildSchedulesKey()` |
| `ConsultationStatusConstant` | 问诊状态常量 |
| `EvaluationStatusConstant` | 评价状态：1-已发布，2-已隐藏 |
| `MessageConstant` | 业务消息文本（"挂号成功"、"支付成功"、"时段"等） |
| `MessageTypeConstant` | 消息类型：1-文本，2-图片 |
| `RoleConstant` | 角色常量：PATIENT、DOCTOR、SUPER_ADMIN、ADMIN |
| `StatusCodeConstant` | HTTP 状态码和业务码：UNAUTHORIZED=401、FORBIDDEN=403 等 |
| `StatusConstant` | 业务状态：排班(1-正常/2-停诊/3-已满)、预约(0-待支付/1-已支付/2-已完成/3-已取消/4-已过期)、审核(0-待审核/1-通过/2-驳回)、账号(0-禁用/1-正常) |

#### 2.2.4 dto 包 — 数据传输对象

| 文件 | 类型 | 用途 |
|------|------|------|
| `LoginDTO.java` | 请求 | 登录入参：phone + password |
| `PatientRegisterDTO.java` | 请求 | 患者注册：name, phone, password, idCard, gender |
| `DoctorRegisterDTO.java` | 请求 | 医生注册：含科室、职称、执业证书等 |
| `AdminRegisterDTO.java` | 请求 | 管理员注册 |
| `AppointmentCreateDTO.java` | 请求 | 预约下单：scheduleId |
| `AppointmentListVO.java` | 响应 | 预约列表 VO：预约号、医生、科室、日期、时段、状态 |
| `ScheduleCreateDTO.java` | 请求 | 排班创建：日期、时段、最大号源数 |
| `ScheduleQueryDTO.java` | 请求 | 排班查询：日期范围 + 状态 |
| `ScheduleCalendarVO.java` | 响应 | 排班日历 VO |
| `ScheduleDetailVO.java` | 响应 | 排班详情：含医生、科室、剩余号源、时段 |
| `DoctorListQueryDTO.java` | 请求 | 医生查询：科室、搜索关键词、分页 |
| `DoctorListVO.java` | 响应 | 医生列表 VO |
| `DoctorHotVO.java` | 响应 | 热门医生排行榜 VO |
| `DoctorPendingVO.java` | 响应 | 待审核医生 VO |
| `DepartmentVO.java` | 响应 | 科室 VO |
| `EvaluationCreateDTO.java` | 请求 | 创建评价：appointmentId, score(1-5), content, isAnonymous |
| `EvaluationListVO.java` | 响应 | 医生评价列表 VO |
| `MyEvaluationVO.java` | 响应 | 我的评价列表 VO |
| `ChatMessageVO.java` | 响应 | WebSocket 消息 VO：含 senderId, senderName, content, sendTime, isSelf |
| `ConsultationRoomVO.java` | 响应 | 问诊室信息 VO |
| `SendMessageDTO.java` | 请求 | WebSocket 消息入参：appointmentId, receiverId, content, msgType |
| `AuditRejectDTO.java` | 请求 | 驳回原因 |
| `OssStsVO.java` | 响应 | OSS STS 临时凭证 |

#### 2.2.5 entity 包 — 数据库实体

| 文件 | 对应表 | 核心字段 |
|------|--------|----------|
| `Admin.java` | admin | username, password(BCrypt), name, role(1-SUPER_ADMIN/2-ADMIN), status |
| `Patient.java` | patient | name, phone(登录账号), password(BCrypt), idCard, gender, status |
| `Doctor.java` | doctor | name, phone, password, departmentId, titleId, status |
| `DoctorAudit.java` | doctor_audit | doctorId, certificateUrl, qualificationUrl, specialty, introduction, auditStatus(0-待审核/1-通过/2-驳回), auditRemark, auditorId |
| `Department.java` | department | name, sortOrder |
| `Title.java` | title | name(主任医师/副主任医师等), sortOrder |
| `Schedule.java` | schedule | doctorId, scheduleDate, period(1-上午/2-下午), maxCount, remainingCount, status |
| `Appointment.java` | appointment | appointmentNo, scheduleId, patientId, doctorId, status(0-待支付/1-已支付/2-已完成/3-已取消) |
| `Evaluation.java` | evaluation | appointmentId, patientId, doctorId, score(1-5), content, isAnonymous, status |
| `ConsultationMessage.java` | consultation_message | appointmentId, senderId, receiverId, senderRole, content, msgType, sendTime |

#### 2.2.6 exception 包 — 异常类（30+）

所有自定义异常均继承 `BusinessException`（或直接 `RuntimeException`），由 `GlobalExceptionHandler` 统一捕获并返回 `Result.error()`。

| 异常类 | 触发场景 |
|--------|----------|
| `AccountNotFoundException` | 登录时账号不存在 |
| `PasswordErrorException` | 密码错误 |
| `AccountDisabledException` | 账号被禁用 |
| `AccountLockedException` | 账号被锁定 |
| `AppointmentNotFoundException` | 预约不存在 |
| `AppointmentDuplicateException` | 重复预约同一排班 |
| `AppointmentTimeoutException` | 支付超时 |
| `AppointmentAlreadyPaidException` | 重复支付 |
| `AppointmentNotPendingException` | 支付时状态非待支付 |
| `AppointmentNotEvaluableException` | 评价时状态不是已支付/已完成 |
| `ScheduleNotFoundException` | 排班不存在 |
| `ScheduleStoppedException` | 排班已停诊 |
| `ScheduleAlreadyFullException` | 号源已满 |
| `ScheduleFullException` | 停诊时号源已满 |
| `ScheduleDuplicateException` | 排班重复 |
| `ScheduleDatePastException` | 排班日期已过 |
| `ScheduleDateNotArrivedException` | 评价时就诊日未到 |
| `ScheduleStatusInvalidException` | 目标状态不合法 |
| `ScheduleHasAppointmentsException` | 删除时有预约 |
| `InsufficientQuotaException` | 号源不足 |
| `DoctorNotFoundException` | 医生不存在 |
| `DoctorAlreadyAuditedException` | 医生已审核过 |
| `DoctorAuditNotFoundException` | 审核数据不存在 |
| `AuditOperationFailedException` | 审核操作失败 |
| `AuditPendingException` | 审核待处理中 |
| `AuditRejectedException` | 审核已驳回 |
| `RejectReasonEmptyException` | 驳回原因为空 |
| `EvaluationNotFoundException` | 评价不存在 |
| `EvaluationDuplicateException` | 重复评价 |
| `EvaluationCreateFailedException` | 评价创建失败 |
| `EvaluationDeleteFailedException` | 评价删除失败 |
| `EvaluationAlreadyDeletedException` | 评价已删除 |
| `PermissionDeniedException` | 权限不足 |
| `PhoneAlreadyExistsException` | 手机号已注册 |
| `UsernameAlreadyExistsException` | 用户名已存在 |
| `PaymentFailedException` | 支付失败 |
| `SystemBusyException` | 系统繁忙（获取锁失败） |
| `SystemException` | 系统异常 |
| `CacheRefreshFailedException` | 缓存刷新失败 |
| `ConsultationException` | 问诊异常 |
| `TitleNotFoundException` | 职称不存在 |
| `DepartmentNotFoundException` | 科室不存在 |

#### 2.2.7 handler 包 — 全局异常处理

**`GlobalExceptionHandler.java`**：`@RestControllerAdvice` 全局异常拦截器
- `BusinessException` → 业务异常，返回自定义 code + msg
- `MethodArgumentNotValidException` → `@Valid` 校验失败，400
- `MethodArgumentTypeMismatchException` → 参数类型转换错误，400
- `HttpMessageNotReadableException` → JSON 解析失败，400
- `ConstraintViolationException` → `@Validated` 校验失败，400
- `NoResourceFoundException` → 404 资源未找到
- `Exception` → 兜底，500 + "系统繁忙，请稍后再试"

#### 2.2.8 interceptor 包 — JWT 拦截器

**`JwtTokenInterceptor.java`**：
- 从 `Authorization` 请求头提取 Token（支持 `Bearer ` 前缀）
- 解析 Token 获取 `userId`、`username`、`role`，存入 `request.setAttribute()`
- 检查方法上的 `@RequireRole` 注解，校验当前用户角色是否在允许列表中
- Token 无效 → 返回 401 JSON
- 权限不足 → 返回 403 JSON

#### 2.2.9 mapper 包 — 通用 Mapper

| 文件 | 类型 | 用途 |
|------|------|------|
| `AppointmentMapper.java` | 注解 SQL | 核心 Mapper：查排班、扣号源（乐观锁）、回滚号源、查预约、分页查患者/医生预约列表、查询超时预约、结束问诊 |
| `DepartmentMapper.java` | MyBatis Mapper | 科室查询 |
| `DoctorAuditMapper.java` | MyBatis Mapper | 审核数据查询（复用于 admin 模块） |
| `DoctorAuthMapper.java` | MyBatis Mapper | 医生认证：按手机/ID 查医生 |
| `PatientAuthMapper.java` | MyBatis Mapper | 患者认证：按手机/ID 查患者 |
| `TitleMapper.java` | MyBatis Mapper | 职称查询 |

**关键 SQL 逻辑 — AppointmentMapper：**
- **号源扣减**（乐观锁）：`UPDATE schedule SET remaining_count - 1 WHERE id = ? AND remaining_count > 0`，若扣减后为 0，状态变 3（已满）
- **号源回滚**：`UPDATE schedule SET remaining_count + 1`，若原状态为 3 且 `remaining_count + 1 > 0` 则恢复为 1（正常）
- **状态更新**（乐观锁）：`UPDATE appointment SET status = ? WHERE id = ? AND status = 0`（仅待支付状态可变更）

#### 2.2.10 service 包 — 通用服务

| 文件 | 用途 |
|------|------|
| `BloomFilterService.java` | 布隆过滤器：`mightContainDoctor()` 判断医生 ID 是否可能存在（防缓存穿透） |
| `LoginAttemptService.java` | 登录失败计数、账号锁定逻辑 |
| `MultiLevelCacheService.java` | 多级缓存抽象：L1 Caffeine + L2 Redis，支持 `TypeReference` 泛型反序列化 |
| `OssStsService.java` | 阿里云 STS 临时凭证生成（用于医生上传证件） |

#### 2.2.11 utils 包 — 工具类

| 文件 | 核心方法 |
|------|----------|
| **`JwtUtil.java`** | `createToken(userId, username, role)` — 生成 HS256 JWT，包含 userId/username/role claims；`parseToken(token)` — 解析校验；`getUserId/getUsername/getRole(token)` — 提取信息；过期时间可配置 `jwt.expiration` |
| **`PasswordUtil.java`** | `encode(rawPassword)` — BCrypt 加密；`matches(rawPassword, encodedPassword)` — 密码匹配 |

#### 2.2.12 result 包 — 统一响应

**`Result.java`**：泛型响应包装类，`Result.success(data)` 返回 `code=1`，`Result.error(code, msg)` 返回自定义错误码和消息。

---

### 2.3 medi-patient 患者端（端口: 8081）

#### 2.3.1 Controller 层

| Controller | 接口路径 | 方法 | 说明 |
|------------|----------|------|------|
| **PatientAuthController** | `POST /patient/login` | login | 患者登录（手机号+密码），返回 JWT Token |
| | `POST /patient/register` | register | 患者注册 |
| **CommonDictController** | `GET /patient/dict/titles` | getTitles | 获取职称字典 |
| **PatientDoctorController** | `GET /patient/departments` | getDepartments | 获取科室列表（缓存 1h） |
| | `GET /patient/doctors` | getDoctorList | 分页搜索医生（多级缓存） |
| | `GET /patient/doctors/hot` | getHotDoctors | 热门医生排行榜（Redis + DB 回源） |
| | `GET /patient/doctors/{doctorId}/schedules` | getScheduleCalendar | 医生排班日历（布隆过滤+多级缓存 1min） |
| | `GET /patient/schedules/{scheduleId}` | getScheduleDetail | 排班详情（含医生、剩余号源） |
| **AppointmentController** | `POST /patient/appointments` | createAppointment | 创建预约（下单锁号） |
| | `GET /patient/appointments` | getMyAppointments | 我的预约列表（分页） |
| | `POST /patient/appointments/{id}/pay` | payAppointment | 模拟支付 |
| **EvaluationController** | `POST /patient/evaluations` | createEvaluation | 提交评价 |
| | `GET /patient/evaluations/my` | getMyEvaluations | 我的评价列表 |
| | `GET /patient/evaluations/doctor/{doctorId}` | getDoctorEvaluations | 医生评价列表 |
| | `DELETE /patient/evaluations/{id}` | deleteEvaluation | 软删除评价 |

#### 2.3.2 Service 层核心业务

**`AppointmentServiceImpl` — 预约核心流程：**
1. **校验排班**：是否存在、日期是否过时、状态是否正常/已停/已满
2. **防重检查**：同一患者+同一排班不能重复预约
3. **分布式锁扣号**：`lock:schedule:{scheduleId}` Redisson 分布式锁（tryLock 3s 等待，10s 持锁）
4. **乐观锁扣减**：`decrementRemainingCount` SQL 带 `AND remaining_count > 0` 条件
5. **插入预约**：生成 `APPOINTMENT_yyyyMMddHHmmss + 4位随机数` 单号
6. **清除医生排班缓存**：`clearScheduleCache(doctorId)`
7. **启动时间轮**：30 分钟后自动检查并取消未支付预约

**`payAppointment` — 支付流程：**
1. 校验预约归属（防止越权支付）
2. 校验状态（未支付且已支付不可重复支付）
3. 校验超时（`createdAt + 30分钟` 与当前时间比较）
4. 校验排班有效性（停诊则拒绝支付）
5. **乐观锁更新**：`UPDATE appointment SET status=1 WHERE id=? AND status=0`

**`AppointmentTimeoutServiceImpl` — 超时回滚：**
- `cancelWithLock(appointmentId)`：加分布式锁后调用事务方法
- `cancelExpiredAppointment(appointmentId)`（`@Transactional`）：
  1. 查询是否仍为待支付且超时
  2. 更新状态为已取消（乐观锁）
  3. `incrementRemainingCount` 回滚号源
  4. 清除排班缓存

> **注意**：采用 `@Lazy` 自注入 `AppointmentTimeoutService self` 避免 `@Transactional` 失效（Spring AOP 代理问题）

**`PatientDoctorServiceImpl` — 多级缓存查询：**
- **科室/职称**：`@Cacheable` Redis 缓存，1 小时 TTL
- **医生列表**：`MultiLevelCacheService`（Caffeine L1 + Redis L2），5 分钟 TTL，使用 `TypeReference` 解决 `PageInfo<T>` 泛型反序列化
- **排班日历**：布隆过滤器 `mightContainDoctor()` 防穿透 → 多级缓存 1 分钟 → 数据库回源

**`EvaluationServiceImpl` — 评价 + 热门排行：**
- **创建评价**：校验预约归属、状态（已支付或已完成）、就诊日已过、无重复 → 保存 → 刷新热门缓存
- **删除评价**：软删除（status=2）
- **热门医生**：`RedisTemplate.opsForValue().get("hot:doctors")` → 缓存命中直接返回 → 未命中查 SQL `findHotDoctors(10)` → 写入 Redis（30 分钟 TTL，空值 5 分钟防穿透）
- **缓存刷新**：评价创建/删除时主动刷新 + `DoctorHotScheduler` 定时每 30 分钟兜底刷新

#### 2.3.3 Mapper 层（本模块特有）

| Mapper | 关键 SQL |
|--------|----------|
| `PatientDoctorMapper.java` + XML | `findAllDepartments`、`findDoctorList`（多表联查：doctor + department + title + audit） |
| `PatientScheduleMapper.java` + XML | `findSchedulesByDoctorIdAndDateRange`（布隆过滤回源） |
| `EvaluationMapper.java` + XML | `findByPatientId`、`findByDoctorId`、`findHotDoctors`（**按医生评分+评价数排行，取 Top 10**）、`countByAppointmentId`、`softDelete` |

#### 2.3.4 定时任务 / 时间轮

| 组件 | 机制 |
|------|------|
| **`AppointmentTimeoutTimer`** | Netty `HashedWheelTimer`（100ms tick, 512 槽位），创建预约后 `scheduleCancel(appointmentId, 30, MINUTES)` 注册延迟任务，到期后调用 `cancelWithLock` 取消并回滚号源；服务关机时 `destroy()` 停止时间轮；**启动时 `@PostConstruct` 扫描历史超时预约自动取消** |
| **`DoctorHotScheduler`** | `@Scheduled(cron = "0 0/30 * * * ?")` 每 30 分钟刷新热门医生 Redis 缓存 |
| **`CacheWarmupRunner`** | `ApplicationRunner`，启动时预热布隆过滤器 |

---

### 2.4 medi-doctor 医生端（端口: 8082）

#### 2.4.1 Controller 层

| Controller | 接口路径 | 方法 | 说明 |
|------------|----------|------|------|
| **DoctorAuthController** | `POST /doctor/login` | login | 医生登录 |
| | `POST /doctor/register` | register | 医生注册 |
| **ScheduleController** | `GET /doctor/schedules/recommend` | getRecommendedMaxCount | 智能推荐号源数 |
| | `POST /doctor/schedules` | createSchedule | 新增排班 |
| | `GET /doctor/schedules` | listSchedules | 排班列表（日期范围+状态筛选） |
| | `PATCH /doctor/schedules/{id}/status` | updateStatus | 停诊/恢复 |
| | `DELETE /doctor/schedules/{id}` | deleteSchedule | 删除排班 |
| **OssController** | `GET /doctor/oss/sts` | getStsToken | 获取 STS 临时凭证 |
| **DoctorAppointmentController** | `GET /doctor/appointments` | getDoctorAppointments | 查看预约列表 |

#### 2.4.2 Service 层核心业务

**`ScheduleServiceImpl` — 排班管理：**
- **`@PostConstruct init()`**：启动时调用 `fixInconsistentStatus()` 修复状态不一致的排班（如实际号源=0 但状态非"已满"）
- **智能推荐** `recommendMaxCount()`：查该医生过去四周同星期几的**历史就诊率** → ≥85% 建议加号 20%（`ceil(maxCount*1.2)`）→ ≤40% 建议减号 20% → 边界保护 1-100
- **创建排班**：防重校验（同日同时段唯一）→ 初始化 `remainingCount = maxCount`，状态=正常
- **停诊/恢复**：仅允许 1↔2 状态切换，号源已满时禁止停诊（需先处理预约）
- **删除排班**：仅无预约记录的排班可删除

#### 2.4.3 Mapper 层

| Mapper | 关键 SQL |
|--------|----------|
| `ScheduleMapper.java` + XML | `countByDoctorDatePeriod`、`findByDoctorIdAndDateRange`、`updateStatus`、`countAppointmentsByScheduleId`、`getHistoricalOccupancyRate`（**按 weekDay 查过去四周平均就诊率**）、`fixInconsistentStatus` |

---

### 2.5 medi-admin 管理端（端口: 8083）

#### 2.5.1 Controller 层

| Controller | 接口路径 | 方法 | 说明 |
|------------|----------|------|------|
| **AdminAuthController** | `POST /admin/login` | login | 管理员登录 |
| | `POST /admin/register` | register | 注册管理员（仅超级管理员） |
| **AdminAuditController** | `GET /admin/doctors/pending` | listPending | 待审核医生列表（分页） |
| | `GET /admin/doctors/{doctorId}/audit-detail` | getAuditDetail | 查看审核详情 |
| | `PATCH /admin/doctors/{doctorId}/approve` | approve | 审核通过 |
| | `PATCH /admin/doctors/{doctorId}/reject` | reject | 审核驳回（需填写原因） |

#### 2.5.2 Service 层 — 审核状态流转

**`AdminAuditServiceImpl`：**

```
医生注册 → doctor 表插入 + doctor_audit 表插入(audit_status=0 待审核)
   │
   ├── approve() → audit_status=1（通过）→ 医生可登录接诊
   │     ├── 校验医生账号未被禁用
   │     └── 校验审核状态必须为 0（待审核），防止重复审核
   │
   └── reject() → audit_status=2（驳回）+ audit_remark=驳回原因
         ├── 校验驳回原因不为空
         └── 校验状态必须为 0
```

#### 2.5.3 Mapper 层

| Mapper | 关键 SQL |
|--------|----------|
| `AdminAuditMapper.java` + XML | `findPendingList`（多表联查：doctor + department + title + doctor_audit）、`findByDoctorId`、`updateAuditStatus` |

---

### 2.6 medi-websocket WebSocket 模块（端口: 8084）

#### 2.6.1 STOMP 配置

**`WebSocketConfig.java`：**
- STOMP 端点：`/ws/chat`（允许所有来源 `*`，注册 `WebSocketHandshakeInterceptor`，启用 SockJS 兼容）
- 消息路由：`/app` 前缀 → `@MessageMapping` 处理
- 消息代理：`/user`（点对点）、`/topic`（广播），`userDestinationPrefix = /user`

**`WebSocketHandshakeInterceptor.java`：**
- WebSocket 握手时从 URL 参数 `token` 和 `appointmentId` 提取信息
- JWT 解析 → 将 `userId`、`role` 存入 `sessionAttributes`
- 记录用户上线（`ConsultationRedisService.userOnline`）

**`WebSocketEventListener.java`：**
- 监听 `SessionDisconnectEvent` → 用户下线时清除 Redis 会话和房间状态

#### 2.6.2 ChatController — 消息处理

**`@MessageMapping("/chat.send")`：**
1. 从 `headerAccessor.getSessionAttributes()` 获取 senderId/senderRole
2. `consultationService.checkConsultationAccess()` 校验预约访问权限（是否属于该问诊室）
3. 校验接收者身份（患者只能发给自己的医生，医生只能发给自己的患者）
4. **防 XSS**：`replaceAll("<", "<").replaceAll(">", ">")`
5. 持久化 `consultation_message` 表
6. **广播到房间**：`/topic/room/{appointmentId}`（按预约 ID 隔离问诊室）
7. **点对点推送**：`/user/{receiverId}/queue/messages`（在线时推送；离线时暂存 Redis 离线消息）

#### 2.6.3 ConsultationRedisService — 会话管理

| 方法 | Redis Key | 用途 |
|------|-----------|------|
| `userOnline(userId, sessionId)` | `ws:user:{userId}` | 记录在线 sessionId，TTL 24h |
| `userOffline(userId)` | `ws:user:{userId}` | 删除在线记录 |
| `isOnline(userId)` | `ws:user:{userId}` | 判断是否在线 |
| `joinRoom(appointmentId, userId)` | `ws:room:{appointmentId}` (Set) | 用户加入问诊室 |
| `leaveRoom(appointmentId, userId)` | `ws:room:{appointmentId}` | 用户离开问诊室 |
| `storeOfflineMessage(userId, message)` | `ws:offline:{userId}` (List) | 暂存离线消息 |
| `getAndClearOfflineMessages(userId)` | `ws:offline:{userId}` | 获取并清空离线消息（上线时补推） |

#### 2.6.4 ConsultationController（REST 接口）

- `GET /consultation/room/{appointmentId}` — 获取问诊室详情
- `GET /consultation/history/{appointmentId}` — 获取聊天历史记录（分页）
- `POST /consultation/end/{appointmentId}` — 结束问诊（更新预约状态为已完成）

---

### 2.7 配置文件与启动类

#### 2.7.1 application.yml 重要配置

各模块通用配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/medi_reserve
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    type: redis

mybatis:
  mapper-locations: classpath:mapper/*.xml

jwt:
  secret: your-256-bit-secret-key-...
  expiration: 86400000  # 24小时

cors:
  allowed-origins: "*"

# 各模块 server.port
# medi-patient: 8081
# medi-doctor: 8082
# medi-admin: 8083
# medi-websocket: 8084
```

**环境变量（.env）**：设 MySQL root 密码、JWT 密钥、阿里云 OSS 密钥

#### 2.7.2 启动类

| 启动类 | 所在模块 | 端口 |
|--------|----------|------|
| `PatientApplication.java` | medi-patient | 8081 |
| `DoctorApplication.java` | medi-doctor | 8082 |
| `AdminApplication.java` | medi-admin | 8083 |
| `WebsocketApplication.java` | medi-websocket | 8084 |

所有启动类均带有 `@SpringBootApplication(scanBasePackages = "com.medireserve")`，扫描范围覆盖 `medi-common`。

---

## 三、前端架构分析

### 3.1 目录结构总览

```
medi-reserve-frontend/
├── index.html
├── package.json
├── vite.config.js          # Vite 配置 + 代理
└── src/
    ├── App.vue             # 根组件
    ├── main.js             # 入口（注册 Element Plus、Pinia、Router）
    ├── api/                # 接口封装（按端划分）
    │   ├── request.js      # Axios 实例 + 拦截器
    │   ├── patient.js      # 患者端 API
    │   ├── doctor.js       # 医生端 API
    │   └── admin.js        # 管理端 API
    ├── router/
    │   └── index.js        # 路由配置 + 守卫
    ├── store/
    │   └── user.js         # Pinia 用户状态
    ├── stores/
    │   └── counter.js      # 示例 store
    ├── utils/
    │   ├── websocket.js    # STOMP + SockJS 封装
    │   └── validate.js     # 校验函数
    ├── components/
    │   └── Layout/
    │       └── index.vue   # 三端共用布局组件
    └── views/
        ├── Login.vue       # 统一登录入口
        ├── patient/        # 患者端页面（11 个）
        ├── doctor/         # 医生端页面（4 个）
        └── admin/          # 管理端页面（3 个）
```

### 3.2 路由与权限

**路由守卫（`router.beforeEach`）：**
1. `to.meta.requiresAuth && !token` → 跳转登录页
2. `to.meta.requiresAuth && token && isTokenExpired(token)` → 清除 Token，提示过期
3. `to.meta.requiresAuth && to.meta.role !== userInfo.role` → 角色不匹配重定向
4. 已登录访问登录/注册页 → 根据角色跳转对应首页

**角色路由映射：**
| 角色 | 路由 meta.role |
|------|---------------|
| PATIENT | `/patient/*` |
| DOCTOR | `/doctor/*` |
| SUPER_ADMIN | `/admin/*` |

### 3.3 页面功能清单

#### 患者端页面

| 页面 | 路由路径 | 业务作用 |
|------|----------|----------|
| `Login.vue` | `/login` | 统一登录（自动按角色分流） |
| `patient/Register.vue` | `/patient/register` | 患者注册 |
| `patient/Home.vue` | `/patient/home` | 首页（科室导航、快捷入口） |
| `patient/Doctors.vue` | `/patient/doctors` | 找医生（按科室/关键词搜索） |
| `patient/Schedule.vue` | `/patient/schedule/:doctorId` | 排班日历（未来 7 天上午/下午） |
| `patient/Confirm.vue` | `/patient/confirm/:scheduleId` | 确认预约（展示排班详情） |
| `patient/Pay.vue` | `/patient/pay/:appointmentId` | 模拟支付（30 分钟倒计时） |
| `patient/Orders.vue` | `/patient/orders` | 我的预约列表（状态筛选、支付/评价/问诊入口） |
| `patient/Evaluate.vue` | `/patient/evaluate/:appointmentId` | 就诊评价（1-5 星 + 文字 + 匿名） |
| `patient/HotDoctors.vue` | `/patient/hot` | 热门医生排行榜 |
| `patient/ChatRoom.vue` | `/patient/chat/:appointmentId?` | 在线问诊（WebSocket 实时消息） |

#### 医生端页面

| 页面 | 路由路径 | 业务作用 |
|------|----------|----------|
| `doctor/Login.vue` | 重定向到 `/login?role=doctor` | 登录 |
| `doctor/Register.vue` | `/doctor/register` | 医生注册（含科室、职称、执业证书） |
| `doctor/ScheduleManage.vue` | `/doctor/schedules` | 排班管理 CRUD + 智能推荐号源 + 停诊/恢复 |
| `doctor/ChatRoom.vue` | `/doctor/chat/:appointmentId?` | 医生端问诊（接收患者消息） |

#### 管理端页面

| 页面 | 路由路径 | 业务作用 |
|------|----------|----------|
| `admin/Login.vue` | 重定向到 `/login?role=admin` | 登录 |
| `admin/Audit.vue` | `/admin/audit` | 医生审核（通过/驳回 + 查看详情） |
| `admin/AdminManage.vue` | `/admin/admins` | 管理员管理（列表 + 状态管理） |

### 3.4 核心组件 / 工具

#### request.js — Axios 封装
- **请求拦截**：自动注入 `Authorization: Bearer {token}`
- **响应拦截**：`code === 1` 成功 → 返回 `res.data`；`code !== 1` → 弹错误提示；HTTP 401 → 清除 Token 跳登录；403 → 权限提示；超时/网络异常 → 友好提示

#### websocket.js — STOMP 封装
- `connect(token, appointmentId, onMessage)`：使用 SockJS 连接 `/ws/chat`，订阅 `/topic/room/{appointmentId}`，支持断线重连（5s）
- `sendMessage(appointmentId, receiverId, content)`：发送到 `/app/chat.send`
- `disconnect()`：断开连接

#### validate.js — 校验函数
- 手机号格式、密码强度、身份证号等正则校验

#### vite.config.js — 代理配置
| 前缀 | 目标 | 说明 |
|------|------|------|
| `/patient` | `localhost:8081` | 患者端 API |
| `/doctor` | `localhost:8082` | 医生端 API |
| `/admin` | `localhost:8083` | 管理端 API |
| `/ws` | `localhost:8084` | WebSocket（ws:true） |
| `/consultation` | `localhost:8084` | 问诊 REST 接口 |

---

## 四、关键业务流程梳理

### 4.1 预约挂号完整链路

```
患者浏览医生 → 点击排班 → 确认预约
    │
    ▼
┌──────────────────────────────────────────────────────┐
│  createAppointment()                                  │
│  ① 校验排班（存在、日期未过、未停诊/未满）             │
│  ② 防重检查（同患者+同排班不可重复）                   │
│  ③ Redisson 分布式锁 lock:schedule:{id}（等3s，持10s）│
│  ④ 乐观锁扣号源：remaining_count - 1 WHERE remaining>0 │
│  ⑤ 插入预约记录（生成单号，status=0 待支付）            │
│  ⑥ 清除医生排班缓存                                    │
│  ⑦ 启动时间轮：30分钟后自动取消                        │
└──────────────────┬───────────────────────────────────┘
                   │ 返回 appointmentId
                   ▼
┌──────────────────────────────────────────────────────┐
│  payAppointment()                                     │
│  ① 校验归属（患者ID匹配）                             │
│  ② 校验未超时（createdAt + 30min < now）              │
│  ③ 校验排班仍然有效                                    │
│  ④ 乐观锁更新：status=1 WHERE status=0                 │
└──────────────────┬───────────────────────────────────┘
                   │ 支付成功
                   ▼
            就诊 → 评价 → 问诊
```

### 4.2 超时取消回滚流程

```
创建预约 → 时间轮注册 30 分钟任务
              │
              ▼ (30 分钟后触发)
    ┌─────────────────────────────────┐
    │  cancelWithLock(appointmentId)  │
    │  ① Redisson 分布式锁            │
    │  ② cancelExpiredAppointment()   │
    └──────────┬──────────────────────┘
               ▼
    ┌─────────────────────────────────┐
    │  @Transactional                 │
    │  ① 查预约是否仍为待支付+超时     │
    │  ② 乐观锁：status=3             │
    │  ③ 回滚号源：remaining+1        │
    │  ④ 清除排班缓存                 │
    └─────────────────────────────────┘

    // 额外保护：应用启动时 @PostConstruct
    // 扫描所有历史超时预约 → 逐个 cancelWithLock
```

### 4.3 热门医生排行计算

```
数据来源：evaluation 表（评分 + 评价数）

┌─ 缓存读取 ──────────────────────────────────────────────┐
│  getHotDoctors()                                       │
│  ① Redis: GET hot:doctors → 命中直接返回                 │
│  ② 未命中 → DB 查询 findHotDoctors(10)                  │
│  ③ 写入 Redis (30min TTL，空值 5min TTL 防穿透)          │
└────────────────────────────────────────────────────────┘

更新时机：
  ├── 评价创建 → 异步刷新缓存
  ├── 评价删除 → 异步刷新缓存
  └── @Scheduled cron("0 0/30 * * * ?") → 每30分钟兜底刷新
```

### 4.4 WebSocket 问诊消息流程

```
患者进入 ChatRoom → connect(token, appointmentId)
    │
    │  WebSocket 握手：/ws/chat?token=xxx&appointmentId=xxx
    │  WebSocketHandshakeInterceptor：JWT解析 → 记录上线
    │  订阅目标频道：/topic/room/{appointmentId}
    │
    ▼ 发送消息
┌──────────────────────────────────────────┐
│  /app/chat.send                          │
│  ① 认证：sessionAttributes 检查 userId/role │
│  ② 权限：checkConsultationAccess()        │
│  ③ 接收者校验（患者↔医生对应关系）           │
│  ④ XSS 过滤                              │
│  ⑤ 持久化 consultation_message            │
│  ⑥ 广播 /topic/room/{appointmentId}       │
│  ⑦ 点对点 /user/{receiver}/queue/messages │
│      ├── 在线 → convertAndSendToUser      │
│      └── 离线 → storeOfflineMessage       │
└──────────────────────────────────────────┘
```

---

## 五、已知问题与约定

### 5.1 已确认的代码约定

| 约定 | 说明 |
|------|------|
| **角色字段名** | 管理员用 `SUPER_ADMIN`（非 `ADMIN`），路由守卫需匹配 |
| **状态码** | `StatusConstant` 使用 `Integer` 类型（非 `int`），需用 `.equals()` 比较 |
| **分页缓存问题** | `PageInfo<T>` 使用多级缓存时需显式传入 `TypeReference`，否则泛型丢失 |
| **自注入模式** | `AppointmentTimeoutServiceImpl` 使用 `@Lazy` 自注入解决 `@Transactional` 失效 |
| **乐观锁** | 预约状态更新、号源扣减均带 `WHERE status = 0` / `WHERE remaining_count > 0` 条件 |

### 5.2 尚未实现/待完善的功能

| 功能 | 当前状态 |
|------|----------|
| **患者个人信息修改** | 未实现（仅有注册和登录） |
| **真实微信支付/支付宝** | 当前为"模拟支付"（直接更新状态） |
| **图片消息** | `msgType` 预留了 2-图片，但前端未上传图片 |
| **医生端预约列表的医生名显示** | `findDoctorAppointments` SQL 中 `p.name as doctorName` 写的是患者名（疑似 Bug） |
| **密码修改** | 未实现 |
| **短信验证码注册** | 未实现（直接手机号+密码注册） |
| **文件上传（医生证件）** | OSS STS 已就绪，但前端上传组件未完整实现 |
| **国际化 i18n** | 未实现 |
| **限流/熔断** | 未配置（高并发场景依赖分布式锁+乐观锁串行化） |

### 5.3 潜在优化点

- **时间轮持久化**：当前使用内存 `HashedWheelTimer`，服务重启后已注册任务丢失（通过 `@PostConstruct` 扫描补偿，但有 30 分钟窗口）
- **缓存一致性**：排班缓存 1 分钟 TTL + 预约后手动清除，基本满足一致性，但分布式多实例场景需通知缓存
- **WebSocket 集群**：当前 STOMP SimpleBroker 为单节点，多实例需引入 RabbitMQ/Redis Pub-Sub 做消息广播

---

## 六、文件清单（附录）

### 后端 — medi-reserve-backend/

```
medi-reserve-backend/
├── pom.xml                                       [核心配置] 父 POM，依赖管理
├── .env                                          [核心配置] 环境变量
├── .env.example                                  [核心配置] 环境变量模板
├── docker-compose.yml                            [核心配置] 容器编排
├── Dockerfile                                    [核心配置] 镜像构建
├── deploy.sh                                     [部署脚本]
├── nginx.conf                                    [核心配置] Nginx 配置
├── medi-reserve.conf                             [核心配置] 域名配置
├── deploy/
│   ├── mysql/init.sql                            [核心配置] 建表+初始数据
│   └── nginx/
│       ├── nginx.conf                            [核心配置]
│       └── conf.d/
│           ├── medi-reserve.conf                 [核心配置]
│           └── medi-reserve-ssl.conf             [核心配置]
│
├── medi-common/pom.xml                           [核心配置]
│   └── src/main/java/com/medireserve/common/
│       ├── annotation/RequireRole.java           [已分析]
│       ├── config/
│       │   ├── BloomFilterConfig.java            [已分析]
│       │   ├── CacheConfig.java                  [已分析]
│       │   ├── DefaultWebMvcConfig.java          [已分析]
│       │   ├── JwtInterceptorProperties.java     [已分析]
│       │   ├── Knife4jConfig.java                [已分析]
│       │   ├── MultiLevelCacheConfig.java        [已分析]
│       │   ├── RedisConfig.java                  [已分析]
│       │   ├── RedissonConfig.java               [已分析]
│       │   └── ResourceConfig.java               [已分析]
│       ├── constant/
│       │   ├── CacheKeyConstants.java            [已分析]
│       │   ├── ConsultationStatusConstant.java   [已分析]
│       │   ├── EvaluationStatusConstant.java     [已分析]
│       │   ├── MessageConstant.java              [已分析]
│       │   ├── MessageTypeConstant.java          [已分析]
│       │   ├── RoleConstant.java                 [已分析]
│       │   ├── StatusCodeConstant.java           [已分析]
│       │   └── StatusConstant.java               [已分析]
│       ├── dto/
│       │   ├── AdminRegisterDTO.java             [已分析]
│       │   ├── AppointmentCreateDTO.java         [已分析]
│       │   ├── AppointmentListVO.java            [已分析]
│       │   ├── AuditRejectDTO.java               [已分析]
│       │   ├── ChatMessageVO.java                [已分析]
│       │   ├── ConsultationRoomVO.java           [已分析]
│       │   ├── DepartmentVO.java                 [已分析]
│       │   ├── DoctorHotVO.java                  [已分析]
│       │   ├── DoctorListQueryDTO.java           [已分析]
│       │   ├── DoctorListVO.java                 [已分析]
│       │   ├── DoctorPendingVO.java              [已分析]
│       │   ├── DoctorRegisterDTO.java            [已分析]
│       │   ├── EvaluationCreateDTO.java          [已分析]
│       │   ├── EvaluationListVO.java             [已分析]
│       │   ├── LoginDTO.java                     [已分析]
│       │   ├── MyEvaluationVO.java               [已分析]
│       │   ├── OssStsVO.java                     [已分析]
│       │   ├── PatientRegisterDTO.java           [已分析]
│       │   ├── ScheduleCalendarVO.java           [已分析]
│       │   ├── ScheduleCreateDTO.java            [已分析]
│       │   ├── ScheduleDetailVO.java             [已分析]
│       │   ├── ScheduleQueryDTO.java             [已分析]
│       │   └── SendMessageDTO.java               [已分析]
│       ├── entity/
│       │   ├── Admin.java                        [已分析]
│       │   ├── Appointment.java                  [已分析]
│       │   ├── ConsultationMessage.java          [已分析]
│       │   ├── Department.java                   [已分析]
│       │   ├── Doctor.java                       [已分析]
│       │   ├── DoctorAudit.java                  [已分析]
│       │   ├── Evaluation.java                   [已分析]
│       │   ├── Patient.java                      [已分析]
│       │   ├── Schedule.java                     [已分析]
│       │   └── Title.java                        [已分析]
│       ├── exception/（30+ 个异常类）              [全部已分析]
│       ├── handler/GlobalExceptionHandler.java   [已分析]
│       ├── interceptor/JwtTokenInterceptor.java   [已分析]
│       ├── mapper/
│       │   ├── AppointmentMapper.java            [已分析]
│       │   ├── DepartmentMapper.java             [已分析]
│       │   ├── DoctorAuditMapper.java            [已分析]
│       │   ├── DoctorAuthMapper.java             [已分析]
│       │   ├── PatientAuthMapper.java            [已分析]
│       │   └── TitleMapper.java                  [已分析]
│       ├── properties/OssProperties.java         [已分析]
│       ├── result/Result.java                    [已分析]
│       ├── service/
│       │   ├── BloomFilterService.java           [已分析]
│       │   ├── LoginAttemptService.java          [已分析]
│       │   ├── MultiLevelCacheService.java       [已分析]
│       │   └── OssStsService.java                [已分析]
│       └── utils/
│           ├── JwtUtil.java                      [已分析]
│           └── PasswordUtil.java                 [已分析]
│
├── medi-patient/pom.xml                          [核心配置]
│   ├── src/main/resources/
│   │   ├── application.yml                       [核心配置]
│   │   ├── application-docker.yml                [核心配置]
│   │   └── mapper/
│   │       ├── EvaluationMapper.xml              [已分析]
│   │       ├── PatientDoctorMapper.xml           [已分析]
│   │       └── PatientScheduleMapper.xml         [已分析]
│   └── src/main/java/com/medireserve/patient/
│       ├── PatientApplication.java               [已分析]
│       ├── controller/
│       │   ├── AppointmentController.java        [已分析]
│       │   ├── CommonDictController.java         [已分析]
│       │   ├── EvaluationController.java         [已分析]
│       │   ├── PatientAuthController.java        [已分析]
│       │   └── PatientDoctorController.java      [已分析]
│       ├── mapper/
│       │   ├── EvaluationMapper.java             [已分析]
│       │   ├── PatientDoctorMapper.java          [已分析]
│       │   └── PatientScheduleMapper.java        [已分析]
│       ├── runner/CacheWarmupRunner.java          [已分析]
│       ├── scheduler/DoctorHotScheduler.java     [已分析]
│       ├── service/
│       │   ├── AppointmentService.java           [已分析]
│       │   ├── AppointmentTimeoutService.java    [已分析]
│       │   ├── CacheEvictService.java            [已分析]
│       │   ├── EvaluationService.java            [已分析]
│       │   ├── PatientAuthService.java           [已分析]
│       │   ├── PatientDoctorService.java         [已分析]
│       │   └── impl/
│       │       ├── AppointmentServiceImpl.java   [已分析]
│       │       ├── AppointmentTimeoutServiceImpl.java [已分析]
│       │       ├── EvaluationServiceImpl.java    [已分析]
│       │       ├── PatientAuthServiceImpl.java   [已分析]
│       │       └── PatientDoctorServiceImpl.java [已分析]
│       └── timer/AppointmentTimeoutTimer.java    [已分析]
│
├── medi-doctor/pom.xml                           [核心配置]
│   ├── src/main/resources/
│   │   ├── application.yml                       [核心配置]
│   │   ├── application-docker.yml                [核心配置]
│   │   └── mapper/ScheduleMapper.xml             [已分析]
│   └── src/main/java/com/medireserve/doctor/
│       ├── DoctorApplication.java                [已分析]
│       ├── controller/
│       │   ├── DoctorAppointmentController.java  [已分析]
│       │   ├── DoctorAuthController.java         [已分析]
│       │   ├── OssController.java                [已分析]
│       │   └── ScheduleController.java           [已分析]
│       ├── mapper/ScheduleMapper.java            [已分析]
│       └── service/
│           ├── DoctorAuthService.java            [已分析]
│           ├── ScheduleService.java              [已分析]
│           └── impl/
│               ├── DoctorAuthServiceImpl.java    [已分析]
│               └── ScheduleServiceImpl.java      [已分析]
│
├── medi-admin/pom.xml                            [核心配置]
│   ├── src/main/resources/
│   │   ├── application.yml                       [核心配置]
│   │   ├── application-docker.yml                [核心配置]
│   │   └── mapper/AdminAuditMapper.xml           [已分析]
│   └── src/main/java/com/medireserve/admin/
│       ├── AdminApplication.java                 [已分析]
│       ├── controller/
│       │   ├── AdminAuditController.java         [已分析]
│       │   └── AdminAuthController.java          [已分析]
│       ├── mapper/
│       │   ├── AdminAuditMapper.java             [已分析]
│       │   └── AdminAuthMapper.java              [已分析]
│       └── service/
│           ├── AdminAuditService.java            [已分析]
│           ├── AdminAuthService.java             [已分析]
│           └── impl/
│               ├── AdminAuditServiceImpl.java    [已分析]
│               └── AdminAuthServiceImpl.java     [已分析]
│
└── medi-websocket/pom.xml                        [核心配置]
    ├── src/main/resources/
    │   ├── application.yml                       [核心配置]
    │   ├── application-docker.yml                [核心配置]
    │   └── mapper/ConsultationMessageMapper.xml  [已分析]
    └── src/main/java/com/medireserve/websocket/
        ├── WebsocketApplication.java             [已分析]
        ├── config/WebSocketConfig.java           [已分析]
        ├── controller/
        │   ├── ChatController.java               [已分析]
        │   └── ConsultationController.java       [已分析]
        ├── interceptor/
        │   └── WebSocketHandshakeInterceptor.java [已分析]
        ├── listener/
        │   └── WebSocketEventListener.java       [已分析]
        ├── mapper/
        │   └── ConsultationMessageMapper.java    [已分析]
        └── service/
            ├── ConsultationRedisService.java     [已分析]
            └── ConsultationService.java          [已分析]
```

### 前端 — medi-reserve-frontend/

```
medi-reserve-frontend/
├── index.html                                    [核心配置]
├── package.json                                  [核心配置]
├── vite.config.js                                [核心配置]
├── jsconfig.json                                 [已分析]
├── README.md                                     [已分析]
├── public/favicon.ico                            [静态资源]
├── .vscode/
│   ├── extensions.json                           [IDE 配置]
│   └── settings.json                             [IDE 配置]
└── src/
    ├── App.vue                                   [已分析]
    ├── main.js                                   [已分析]
    ├── api/
    │   ├── request.js                            [已分析]
    │   ├── patient.js                            [已分析]
    │   ├── doctor.js                             [已分析]
    │   └── admin.js                              [已分析]
    ├── router/index.js                           [已分析]
    ├── store/user.js                             [已分析]
    ├── stores/counter.js                         [示例代码]
    ├── utils/
    │   ├── websocket.js                          [已分析]
    │   └── validate.js                           [已分析]
    ├── components/Layout/index.vue               [已分析]
    └── views/
        ├── Login.vue                             [已分析]
        ├── patient/
        │   ├── Login.vue                         [已分析]
        │   ├── Register.vue                      [已分析]
        │   ├── Home.vue                          [已分析]
        │   ├── Doctors.vue                       [已分析]
        │   ├── Schedule.vue                      [已分析]
        │   ├── Confirm.vue                       [已分析]
        │   ├── Pay.vue                           [已分析]
        │   ├── Orders.vue                        [已分析]
        │   ├── Evaluate.vue                      [已分析]
        │   ├── HotDoctors.vue                    [已分析]
        │   └── ChatRoom.vue                      [已分析]
        ├── doctor/
        │   ├── Login.vue                         [已分析]
        │   ├── Register.vue                      [已分析]
        │   ├── ScheduleManage.vue                [已分析]
        │   └── ChatRoom.vue                      [已分析]
        └── admin/
            ├── Login.vue                         [已分析]
            ├── Audit.vue                         [已分析]
            └── AdminManage.vue                   [已分析]
```

**总计：配置文件 15 个 + Java 源文件约 100 个 + Vue 组件 30 个 = 约 145 个文件，全部已分析。**

---

> **报告完成。** 本报告基于对项目源代码的全面扫描，覆盖了所有 Java 类、Vue 组件、配置文件、SQL 脚本和部署文件。如有遗漏或需补充特定模块细节，请随时告知。