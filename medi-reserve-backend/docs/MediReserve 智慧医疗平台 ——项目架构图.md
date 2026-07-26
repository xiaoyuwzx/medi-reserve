# MediReserve 智慧医疗预约挂号平台 — 项目架构图 

> **版本**：v1.0-SNAPSHOT  
> **最后更新**：2026-07-23

---

## 目录

1. [整体架构图](#一整整体架构图)
2. [技术栈清单](#二技术栈清单)
3. [核心业务流程时序图：预约挂号](#三核心业务流程时序图预约挂号)
4. [缓存架构图](#四缓存架构图)
5. [WebSocket 消息流图](#五websocket-消息流图)
6. [项目模块依赖关系](#六项目模块依赖关系)
7. [安全架构](#七安全架构)

---

## 一、整体架构图

```mermaid
graph TD
    subgraph "前端 Frontend"
        PatientFE[患者端 Vue3 + Element Plus]
        DoctorFE[医生端 Vue3 + Element Plus]
        AdminFE[管理端 Vue3 + Element Plus]
    end

    subgraph "反向代理 (可选，当前注释)"
        Nginx[Nginx :80/:443]
    end

    subgraph "后端微服务 Backend Services"
        PatientSvc["患者端 :8081<br/>medi-patient<br/>注册/登录/预约/支付/评价"]
        DoctorSvc["医生端 :8082<br/>medi-doctor<br/>注册/排班/问诊/统计/OSS上传"]
        AdminSvc["管理端 :8083<br/>medi-admin<br/>审核/统计看板/日志/权限管理"]
        WsSvc["WebSocket :8084<br/>medi-websocket<br/>在线问诊/实时消息"]
    end

    subgraph "公共模块 Common"
        CommonModule["medi-common<br/>实体类 / DTO / Mapper<br/>JWT工具 / 全局异常 / 拦截器<br/>多级缓存 / 布隆过滤器<br/>分布式锁配置"]
    end

    subgraph "中间件 Middleware"
        MySQL[(MySQL 8.0<br/>:3306)]
        Redis[(Redis 7.0<br/>:6379)]
        OSS[阿里云 OSS<br/>医生证件存储]
    end

    subgraph "外部 External"
        WeChatPay[微信支付<br/>当前模拟]
    end

    %% 前端到反向代理
    PatientFE --> Nginx
    DoctorFE --> Nginx
    AdminFE --> Nginx

    %% 反向代理到后端（当前直连）
    PatientFE --> PatientSvc
    DoctorFE --> DoctorSvc
    AdminFE --> AdminSvc
    PatientFE -.->|WebSocket STOMP| WsSvc
    DoctorFE -.->|WebSocket STOMP| WsSvc

    %% 各服务依赖公共模块（代码级引用，非HTTP调用）
    PatientSvc -.->|Maven依赖| CommonModule
    DoctorSvc -.->|Maven依赖| CommonModule
    AdminSvc -.->|Maven依赖| CommonModule
    WsSvc -.->|Maven依赖| CommonModule

    %% 数据层
    PatientSvc --> MySQL
    PatientSvc --> Redis
    DoctorSvc --> MySQL
    DoctorSvc --> Redis
    DoctorSvc --> OSS
    AdminSvc --> MySQL
    AdminSvc --> Redis
    WsSvc --> MySQL
    WsSvc --> Redis

    %% 外部支付
    PatientSvc -.->|模拟| WeChatPay

    style CommonModule fill:#f9f,stroke:#333,stroke-width:2px
    style MySQL fill:#4479A1,color:#fff
    style Redis fill:#DC382D,color:#fff
    style OSS fill:#FF6A00,color:#fff
```

---

## 二、技术栈清单

### 2.1 后端核心技术

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **框架** | Spring Boot | 3.3.6 | 应用基础框架 |
| **语言** | Java | 17 | 编程语言 |
| **ORM** | MyBatis + Spring Boot Starter | 3.0.3 | 数据库访问 |
| **分页** | PageHelper Spring Boot | 2.1.0 | 分页查询 |
| **数据库** | MySQL | 8.0 | 关系型数据库 |
| **缓存 L1** | Caffeine | 3.1.8 | 本地缓存（热点数据） |
| **缓存 L2** | Redis (Lettuce) | 7.0 | 分布式缓存 |
| **布隆过滤器** | Guava BloomFilter | 33.2.0-jre | 防缓存穿透 |
| **分布式锁** | Redisson | 3.27.2 | Redis 分布式锁 + 延迟队列 |
| **定时器** | Netty HashedWheelTimer | 4.1.115 | 预约超时取消（时间轮算法） |
| **WebSocket** | Spring WebSocket + STOMP | - | 在线问诊实时通信 |
| **WebSocket 兼容** | SockJS | - | 兼容不原生支持 WebSocket 的浏览器 |
| **认证** | JJWT (io.jsonwebtoken) | 0.12.5 | JWT Token 签发与验证 |
| **密码加密** | Spring Security Crypto | 6.3.5 | BCrypt 密码哈希 |
| **API 文档** | Knife4j (OpenAPI 3.0) | 4.5.0 | 在线 API 文档 + Swagger UI |
| **工具** | Hutool | 5.8.26 | 通用工具类 |
| **简化代码** | Lombok | 1.18.34 | 消除模板代码 |
| **对象存储** | 阿里云 OSS SDK | 3.17.4 | 医生证件文件存储 |
| **STS 认证** | 阿里云 STS SDK | 3.1.1 | 临时凭证安全上传 |

### 2.2 容器化与运维

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **构建** | Maven | 3.9.6 | 依赖管理与构建 |
| **容器化** | Docker + Docker Compose | 2.x | 多服务编排 |
| **基础镜像** | eclipse-temurin:17-jre-alpine | - | 轻量级 JRE 镜像 |
| **构建镜像** | maven:3.9.6-eclipse-temurin-17 | - | 多阶段构建 |
| **反向代理** | Nginx Alpine | - | (当前已注释，待启用) |
| **健康检查** | Spring Boot Actuator | - | 容器健康状态检测 |

### 2.3 前端技术（参考）

| 类别 | 技术 | 用途 |
|------|------|------|
| **框架** | Vue 3 | SPA 前端框架 |
| **UI 库** | Element Plus | 组件库 |
| **WebSocket 客户端** | STOMP.js + SockJS | 在线问诊实时通信 |

---

## 三、核心业务流程时序图：预约挂号

> 流程包含：**分布式锁（Redisson）** + **数据库乐观锁** + **Netty 时间轮超时取消**

```mermaid
sequenceDiagram
    actor 患者 as Patient
    participant Controller as AppointmentController
    participant Service as AppointmentService
    participant Redisson as RedissonClient
    participant Redis as Redis
    participant MySQL as MySQL
    participant Timer as Netty HashedWheelTimer
    participant Cache as Caffeine/Redis Cache

    患者->>Controller: POST /patient/appointments<br/>{scheduleId: 101}
    Controller->>Service: createAppointment(patientId, dto)

    Note over Service: Step 1: 业务校验
    Service->>MySQL: SELECT * FROM schedule WHERE id=101
    MySQL-->>Service: schedule (status, remaining_count)
    Service->>Service: 校验排班日期、状态、是否重复预约

    Note over Service,Redisson: Step 2: 获取分布式锁
    Service->>Redisson: getLock("lock:schedule:101")
    Service->>Redis: tryLock(wait=3s, lease=15s)
    alt 锁获取失败
        Redis-->>Service: false
        Service-->>Controller: 系统繁忙，请稍后重试
        Controller-->>患者: 系统繁忙
    end

    Note over Service,MySQL: Step 3: 数据库乐观锁扣减号源
    Service->>MySQL: UPDATE schedule<br/>SET remaining_count = remaining_count - 1<br/>WHERE id = 101 AND remaining_count > 0
    alt remaining_count = 0
        MySQL-->>Service: affected_rows = 0
        Service-->>Controller: 号源不足
    else success
        MySQL-->>Service: affected_rows = 1
    end

    Note over Service: Step 4: 插入预约记录
    Service->>MySQL: INSERT INTO appointment<br/>(appointment_no, schedule_id, patient_id, doctor_id, status=0)

    Note over Service,Cache: Step 5: 清除排班缓存
    Service->>Cache: clearScheduleCache(doctorId)

    Note over Service,Timer: Step 6: 启动 30 分钟超时倒计时
    Service->>Timer: scheduleCancel(appointmentId, 30min)
    Timer-->>Timer: 30分钟后回调检查<br/>若状态仍为待支付 → 取消 + 回滚号源

    Note over Service,Redisson: Step 7: 释放分布式锁
    Service->>Redisson: unlock()

    Service-->>Controller: appointment (id, appointmentNo, status=0)
    Controller-->>患者: 预约成功<br/>预约单号: APPOINTMENT_xxx<br/>请在30分钟内支付
```

### 预约状态流转

```mermaid
stateDiagram-v2
    [*] --> 待支付: 创建预约
    待支付 --> 已支付: 用户支付
    待支付 --> 已取消: 30分钟超时自动取消
    待支付 --> 已过期: 排班日期已过
    已支付 --> 已就诊: 问诊结束
    已支付 --> 已取消: 排班停诊/手动取消
    已就诊 --> 已评价: 用户提交评价
```

---

## 四、缓存架构图

> 多级缓存架构：**Caffeine L1（本地）** + **Redis L2（分布式）** + **布隆过滤器（防穿透）**

```mermaid
graph TB
    subgraph "请求层"
        Client[前端请求]
    end

    subgraph "布隆过滤器 Bloom Filter"
        BF_Doctor[医生ID布隆过滤器<br/>Guava BloomFilter<Long><br/>预期插入: 10000, 误判率: 1%]
        BF_Schedule[排班ID布隆过滤器<br/>Guava BloomFilter<Long><br/>预期插入: 1,000,000, 误判率: 1%]
    end

    subgraph "L1 本地缓存 Caffeine"
        CaffeineMgr[CaffeineCacheManager<br/>过期时间: 1小时<br/>最大条数: 1000<br/>允许空值缓存 (防穿透)]
    end

    subgraph "L2 分布式缓存 Redis"
        RedisMgr[RedisCacheManager<br/>过期时间: 5分钟<br/>跨实例共享]
    end

    subgraph "数据库 MySQL"
        DB[(MySQL 8.0)]
    end

    Client --> BF_Doctor
    Client --> BF_Schedule
    BF_Doctor -->|可能存在| CaffeineMgr
    BF_Doctor -->|一定不存在| NotFound[返回空/不存在]
    BF_Schedule -->|可能存在| CaffeineMgr
    BF_Schedule -->|一定不存在| NotFound

    CaffeineMgr -->|Miss| RedisMgr
    CaffeineMgr -->|Hit| Return1[直接返回]
    RedisMgr -->|Miss| DB
    RedisMgr -->|Hit| Return2[回写 Caffeine 后返回]
    DB -->|Found| Return3[回写 Redis → Caffeine 后返回]
    DB -->|Not Found| CacheNull[缓存空值到 Caffeine<br/>防缓存穿透]

    style BF_Doctor fill:#FF9800,color:#fff
    style BF_Schedule fill:#FF9800,color:#fff
    style CaffeineMgr fill:#4CAF50,color:#fff
    style RedisMgr fill:#DC382D,color:#fff
    style DB fill:#4479A1,color:#fff
```

### 缓存使用场景

| 缓存数据 | L1 (Caffeine) | L2 (Redis) | 布隆过滤器 | 刷新策略 |
|---------|--------------|------------|-----------|---------|
| 医生信息 | ✅ 热点数据 | ✅ 跨实例共享 | ✅ 防穿透 | 排班变化时清除 |
| 排班日历 | ✅ 未来7天 | — | ✅ 防穿透 | 创建预约时清除 |
| 热门医生排行 | — | ✅ 手动刷新 | — | 管理员手动触发 |
| 科室/职称字典 | ✅ 静态数据 | — | — | 基本不变 |

---

## 五、WebSocket 消息流图

```mermaid
sequenceDiagram
    actor Patient as 患者
    actor Doctor as 医生
    participant WS as WebSocket Server<br/>:8084 (STOMP)
    participant Handler as HandshakeInterceptor
    participant ChatCtrl as ChatController
    participant RoomSvc as ConsultationService
    participant DB as MySQL
    participant Redis as Redis
    participant Broker as SimpleBroker<br/>/topic /user

    Note over Patient,Doctor: ====== 阶段1：连接建立与握手认证 ======

    Patient->>WS: CONNECT /ws/chat<br/>Header: Authorization Bearer xxx
    WS->>Handler: 拦截握手请求
    Handler->>Handler: 解析 Token → userId, role
    Handler->>Handler: 校验 Token 有效性
    alt Token 无效
        Handler-->>WS: 拒绝连接
        WS-->>Patient: 401 Unauthorized
    else Token 有效
        Handler->>WS: 设置 Session Attributes<br/>{userId: 1, role: "PATIENT"}
        WS-->>Patient: CONNECTED ✓
    end

    Patient->>WS: SUBSCRIBE /topic/room/1001
    WS-->>Patient: SUBSCRIBED ✓

    Doctor->>WS: CONNECT /ws/chat
    WS->>Handler: 握手认证
    Handler->>WS: {userId: 5, role: "DOCTOR"}
    WS-->>Doctor: CONNECTED ✓

    Doctor->>WS: SUBSCRIBE /topic/room/1001
    WS-->>Doctor: SUBSCRIBED ✓

    Note over Patient,Doctor: ====== 阶段2：消息发送 ======

    Patient->>WS: SEND /app/chat.send<br/>{appointmentId: 1001, receiverId: 5, content: "你好医生"}
    WS->>ChatCtrl: @MessageMapping("/chat.send")

    Note over ChatCtrl: Step 1: 校验咨询权限
    ChatCtrl->>RoomSvc: checkConsultationAccess(1001, patientId, "PATIENT")
    RoomSvc->>DB: SELECT appointment WHERE id=1001
    RoomSvc-->>ChatCtrl: appointment (验证患者/医生归属)

    Note over ChatCtrl: Step 2: XSS 过滤
    ChatCtrl->>ChatCtrl: content.replaceAll("<", "<")<br/>content.replaceAll(">", ">")

    Note over ChatCtrl: Step 3: 持久化存储
    ChatCtrl->>DB: INSERT INTO consultation_message<br/>(appointment_id, sender_id, receiver_id,<br/>sender_role, content, send_time)

    Note over ChatCtrl: Step 4: 查询发送者姓名
    ChatCtrl->>DB: SELECT name FROM patient WHERE id=1
    DB-->>ChatCtrl: "张三"

    Note over ChatCtrl,Broker: Step 5: 广播到房间频道
    ChatCtrl->>Broker: convertAndSend("/topic/room/1001", chatMessageVO)
    Broker-->>Patient: MESSAGE {senderName:"张三", content:"你好医生"}
    Broker-->>Doctor: MESSAGE {senderName:"张三", content:"你好医生"}

    Note over ChatCtrl,Redis: Step 6: 离线消息处理
    ChatCtrl->>Redis: isOnline(receiverId: 5)?
    alt 接收者在线
        Redis-->>ChatCtrl: true (在线)
        ChatCtrl->>ChatCtrl: 跳过离线消息推送
    else 接收者离线
        Redis-->>ChatCtrl: false (离线)
        ChatCtrl->>Redis: storeOfflineMessage(receiverId: 5, message)
        Note over Redis: 离线消息暂存<br/>(接收者上线后补发)
    end
```

### WebSocket 架构要点

| 特性 | 实现方式 |
|------|---------|
| **协议** | STOMP over WebSocket |
| **连接端点** | `/ws/chat` (支持 SockJS 兼容) |
| **应用前缀** | `/app` → `@MessageMapping` 处理 |
| **广播频道** | `/topic/room/{appointmentId}` — 按预约 ID 隔离房间 |
| **点对点** | `/user/{userId}/queue/messages` — 备用推送 |
| **认证方式** | 握手拦截器解析 JWT Token → Session Attributes |
| **XSS 防御** | 服务端转义 `<` 和 `>` |
| **离线消息** | Redis 暂存，接收者上线后补发 |
| **消息持久化** | 同步写入 MySQL `consultation_message` 表 |

---

## 六、项目模块依赖关系

```mermaid
graph TD
    subgraph "父工程 medi-reserve"
        Parent[pom.xml<br/>groupId: com.medireserve<br/>version: 1.0-SNAPSHOT<br/>packaging: pom]
    end

    subgraph "公共模块"
        Common["medi-common<br/>---<br/>entity (14个实体)<br/>dto (40+个传输对象)<br/>mapper (6个公共Mapper)<br/>config (缓存/Redis/Redisson/布隆/WebMVC/Knife4j)<br/>constant (状态码/角色/消息常量)<br/>exception (40+个业务异常)<br/>interceptor (JWT拦截器)<br/>utils (JWT/密码工具)<br/>service (STS/权限/登录尝试/服务)"]
    end

    subgraph "业务模块"
        Patient["medi-patient :8081<br/>---<br/>controller (5个)<br/>service (6个实现)<br/>timer (Netty超时定时器)<br/>mapper (3个XML)"]
        Doctor["medi-doctor :8082<br/>---<br/>controller (5个)<br/>service (5个实现)<br/>mapper (2个XML)"]
        Admin["medi-admin :8083<br/>---<br/>controller (5个)<br/>service (5个实现)<br/>mapper (5个XML)"]
        WebSocket["medi-websocket :8084<br/>---<br/>controller (2个)<br/>service (3个实现)<br/>config (WebSocket配置)<br/>interceptor (握手拦截器)<br/>mapper (1个XML)"]
    end

    Parent --> Common
    Parent --> Patient
    Parent --> Doctor
    Parent --> Admin
    Parent --> WebSocket

    Patient -->|Maven compile| Common
    Doctor -->|Maven compile| Common
    Admin -->|Maven compile| Common
    WebSocket -->|Maven compile| Common

    style Common fill:#f9f,stroke:#333,stroke-width:2px
    style Parent fill:#888,color:#fff
```

### 模块职责说明

| 模块 | 职责 | 是否可独立运行 | 依赖 |
|------|------|--------------|------|
| `medi-common` | 公共代码库（实体、DTO、工具、配置） | ❌ (仅编译依赖) | — |
| `medi-patient` | 患者端业务逻辑 | ✅ | medi-common |
| `medi-doctor` | 医生端业务逻辑 + OSS | ✅ | medi-common |
| `medi-admin` | 管理端业务逻辑 + RBAC | ✅ | medi-common |
| `medi-websocket` | WebSocket 实时通信 | ✅ | medi-common |

> **说明**：各模块之间不通过 HTTP 相互调用，而是通过共享 `medi-common` 模块完成代码复用。模块间数据共享通过共同访问同一个 MySQL 数据库和 Redis 实例实现。

---

## 七、安全架构

### 7.1 认证流程

```mermaid
sequenceDiagram
    actor User
    participant Login as 登录接口<br/>(/login)
    participant Service as AuthService
    participant DB as MySQL
    participant JWT as JwtUtil

    User->>Login: POST /patient/login<br/>{phone, password}
    Login->>Service: login(phone, password)
    Service->>DB: SELECT * FROM patient WHERE phone=?
    DB-->>Service: patient (含加密密码)
    Service->>Service: BCrypt.matches(password, encryptedPassword)
    Service->>JWT: createToken(id, phone, role="PATIENT")
    JWT-->>Service: token (有效期2h)
    Service-->>Login: patient + token
    Login-->>User: {token, id, name, phone}

    Note over User,Login: ====== 后续请求携带 Token ======
    User->>+Filter: GET /patient/appointments<br/>Header: Authorization Bearer xxx
    Filter->>JWT: parseToken(token)
    JWT-->>Filter: {userId, role}
    Filter->>Filter: 设置 Request Attributes
    Note over Filter: 跳过白名单路径<br/>(/login, /register, /dict/**, /doc.html 等)
```

### 7.2 权限控制体系

| 层次 | 实现 | 说明 |
|------|------|------|
| **JWT 拦截器** | `JwtTokenInterceptor` | 验证 Token 有效性，解析 userId/role |
| **角色注解** | `@RequireRole({ROLE})` | 方法级角色校验（PATIENT / DOCTOR / SUPER_ADMIN / ADMIN） |
| **权限注解** | `@RequirePermission("code")` | 方法级权限校验（RBAC 权限点） |
| **操作日志** | `@LogOperation` + AOP | 自动记录管理员操作到 `operation_log` 表 |
| **密码加密** | BCryptPasswordEncoder | 所有用户密码 BCrypt 加密存储 |

### 7.3 角色权限矩阵

| 角色 | 可访问模块 | 默认权限 |
|------|-----------|---------|
| PATIENT | 患者端 | 自身数据读写 |
| DOCTOR | 医生端 | 自身数据读写 + 排班管理 |
| SUPER_ADMIN (role=1) | 管理端 | 所有权限（13条全开） |
| ADMIN (role=2) | 管理端 | 查看权限（4条只读） |

---

> **更多信息**：请参阅 [部署文档](DEPLOY.md)、[用户手册](USER_MANUAL.md)、[数据库ER图](ER_DIAGRAM.md)。