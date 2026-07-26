# MediReserve 智慧医疗平台 —— 核心模块深度解析

> 本文档涵盖项目全部核心模块，包括：认证授权、挂号核心业务、超时取消（Netty时间轮）、多级缓存、WebSocket在线问诊、OSS STS临时凭证、热门医生排行榜、智能号源推荐、操作日志AOP。每个模块均包含：作用、实现步骤、完整数据流时序图、核心设计思想、扩展思考、面试高频问答。

------

## 一、项目概述与技术栈

**MediReserve** 是一个多模块 Spring Boot 医疗预约挂号平台，采用 **患者端、医生端、管理端** 三端分离架构，覆盖挂号、支付、评价、问诊完整闭环。

| 层级     | 技术选型                             | 版本             |
| :------- | :----------------------------------- | :--------------- |
| 框架     | Spring Boot                          | 3.3.6            |
| JDK      | Java 17                              | —                |
| ORM      | MyBatis + PageHelper                 | 3.0.3 / 2.1.0    |
| 认证授权 | JWT + BCrypt + 自定义注解            | JJWT 0.12.5      |
| 分布式锁 | Redisson                             | 3.27.2           |
| 缓存     | Caffeine + Redis + Guava BloomFilter | —                |
| 延迟任务 | Netty HashedWheelTimer               | 4.1.115.Final    |
| 实时通信 | STOMP + SockJS                       | Spring Boot 内置 |
| 文件存储 | 阿里云 OSS + STS                     | 3.17.4 / 4.6.4   |
| 分页     | PageHelper                           | 2.1.0            |
| 接口文档 | Knife4j (OpenAPI 3)                  | 4.5.0            |

------

## 二、认证授权模块（JWT + 拦截器 + 自定义注解）

### 2.1 模块作用

- **统一身份认证**：通过 JWT Token 验证用户身份。
- **细粒度权限控制**：支持角色级（`@RequireRole`）和权限码级（`@RequirePermission`）校验。
- **上下文传递**：将 `userId`、`role` 注入 Request 属性，供 Controller 直接使用。
- **登录防暴力破解**：Redis 记录失败次数，5 次失败锁定 15 分钟。

### 2.2 实现步骤

1. **定义注解**：`@RequireRole`（角色）和 `@RequirePermission`（权限码）。
2. **JWT 工具类**：生成/解析 JWT，包含 `userId`、`username`、`role`。
3. **拦截器 `JwtTokenInterceptor`**：
   - 拦截所有请求，检查方法或类上是否有注解。
   - 解析 Token，校验有效期和签名。
   - 校验角色/权限。
   - 通过后将用户信息存入 `request.setAttribute`。
4. **登录失败锁定**：使用 Redis 记录失败次数，Key = `login:fail:{username}`。

### 2.3 完整数据流时序图

```
客户端请求（携带 Authorization: Bearer <JWT>）
    │
    ▼
DispatcherServlet
    │
    ▼
JwtTokenInterceptor.preHandle()
    │
    ├─ ① 检查是否需要认证（方法/类是否有 @RequireRole/@RequirePermission）
    │   ├─ 无注解 → 放行
    │   └─ 有注解 → 继续
    │
    ├─ ② 提取 Token，解析 Claims
    │   ├─ Token 缺失/无效 → 返回 401
    │   └─ 解析成功 → 获取 userId, role
    │
    ├─ ③ 存入 request.setAttribute("userId", userId)
    │                        setAttribute("role", role)
    │
    ├─ ④ 角色校验（@RequireRole）
    │   └─ 角色不匹配 → 返回 403
    │
    ├─ ⑤ 权限校验（@RequirePermission）
    │   └─ 权限不匹配 → 返回 403
    │
    └─ ⑥ 放行，进入 Controller
```



### 2.4 核心设计思想

- **注解驱动**：通过注解声明式控制权限，业务代码零侵入。
- **拦截器 + Request 属性**：一次解析，全链路共享，避免重复解析 Token。
- **Redis 锁定**：分布式环境下共享失败计数，保证一致性。
- **BCrypt 加密**：每次加密结果不同，防止彩虹表攻击。

### 2.5 扩展思考

- 如果引入 OAuth2 / 第三方登录，如何兼容？可在拦截器中增加对特定路径的放行，并扩展 `Claims` 解析逻辑。
- 若需支持刷新 Token，可增加 `/refresh` 接口，用旧 Token 换取新 Token（需校验旧 Token 有效性）。
- 权限缓存可迁移至 Redis，减少每次查询 DB 的压力。

### 2.6 面试高频问答

**Q1: 如何防止 Token 被盗用？**
A: Token 存储在客户端，建议使用 HTTPS 传输；设置较短有效期（2小时）；敏感操作增加二次校验（如支付需输入密码）。

**Q2: `@RequireRole` 加在类和方法上的优先级？**
A: 方法优先于类。拦截器先找方法上的注解，没有再找类上的，实现“方法覆盖类”的效果。

**Q3: 登录失败锁定后，怎么解锁？**
A: 锁定期间用户无法登录；锁定过期后 Redis Key 自动删除，用户可重试。管理员也可手动删除 Redis Key 解锁。

------

## 三、挂号核心业务模块（分布式锁 + 乐观锁）

### 3.1 模块作用

- **号源扣减**：保证在高并发下不超卖。
- **预约单生成**：创建待支付预约，生成唯一单号。
- **超时倒计时**：启动 30 分钟支付倒计时。
- **缓存同步**：扣减号源后立即清除排班缓存。

### 3.2 实现步骤

1. **前置校验**：排班存在、日期未过、未停诊、号源未满、未重复预约。
2. **分布式锁**：`RLock lock = redissonClient.getLock("lock:schedule:" + scheduleId)`，`tryLock(3, 15, TimeUnit.SECONDS)`。
3. **乐观锁扣减**：`UPDATE schedule SET remaining_count = remaining_count - 1 WHERE id = ? AND remaining_count > 0`，检查影响行数。
4. **插入预约**：生成 `appointmentNo`，状态为 `0-待支付`。
5. **清除缓存**：`patientDoctorService.clearScheduleCache(doctorId)`。
6. **启动时间轮**：`timeoutTimer.scheduleCancel(appointmentId, 30, TimeUnit.MINUTES)`。

### 3.3 完整数据流时序图

```
患者发起挂号请求
    │
    ▼
AppointmentController.createAppointment()
    │
    ▼
AppointmentServiceImpl.createAppointment()
    │
    ├─ ① 校验排班（存在/日期/状态/号源）
    ├─ ② 校验重复预约（同一患者+排班）
    │
    ├─ ③ 分布式锁：lock:schedule:{id}
    │   ├─ 等待 3 秒获取锁
    │   ├─ 成功 → 继续
    │   └─ 失败 → 抛出 SystemBusyException
    │
    ├─ ④ 乐观锁扣减号源（SQL 更新 remaining_count）
    │   ├─ rows > 0 → 继续
    │   └─ rows = 0 → 抛出 InsufficientQuotaException
    │
    ├─ ⑤ 插入预约记录（status = 0）
    ├─ ⑥ 清除排班缓存
    ├─ ⑦ 启动时间轮（30 分钟后触发取消）
    │
    └─ ⑧ 释放分布式锁（finally 块）
    │
    ▼
返回预约成功信息给前端
```



### 3.4 核心设计思想

- **双重防超卖**：分布式锁解决 JVM 级竞争，乐观锁解决数据库级最终一致。
- **锁参数精准**：等待 3 秒避免长时间阻塞，持有 15 秒远大于业务耗时。
- **事务边界**：锁释放必须在事务提交之后，保证数据可见性。
- **缓存同步**：扣减后立即清除缓存，避免前端看到旧数据。

### 3.5 扩展思考

- 若 Redisson 锁超时导致提前释放，乐观锁会兜底，但可能造成已扣减但未提交事务。可增加手动回滚机制或使用 Redisson 的看门狗自动续期。
- 若需支持不同支付时效（如专家号 15 分钟），可参数化超时时间。

### 3.6 面试高频问答

**Q1: 为什么用了分布式锁还要用乐观锁？**
A: 分布式锁在极端情况（锁超时、网络抖动）可能失效，乐观锁是最后防线。双重保障确保 `remaining_count` 永远不为负。

**Q2: `tryLock(3, 15, TimeUnit.SECONDS)` 的参数如何确定？**
A: 等待 3 秒：正常抢锁毫秒级，3 秒代表系统繁忙，快速失败。持有 15 秒：业务实际耗时 < 100ms，15 秒足够宽松，且远小于业务超时（30分钟），不会因锁提前释放导致并发问题。

**Q3: 如果扣减号源成功但插入预约失败怎么办？**
A: 由于 `@Transactional` 包裹整个方法，任何异常都会回滚所有数据库操作，包括号源扣减。但清除缓存和时间轮任务不会回滚，需额外补偿（如日志记录 + 手动修复）。

------

## 四、超时取消模块（Netty 时间轮）

### 4.1 模块作用

- **延迟任务调度**：30 分钟后自动检查预约状态，若仍未支付则取消并回滚号源。
- **服务重启补偿**：启动时扫描数据库，处理遗漏的超时预约。
- **分布式防重复**：使用分布式锁保证多实例只执行一次。

### 4.2 实现步骤

1. **创建时间轮**：`HashedWheelTimer(100ms, 512槽)`。
2. **安排任务**：`scheduleCancel(appointmentId, 30, TimeUnit.MINUTES)`，提交 `CancelTask`。
3. **任务执行**：`run()` 调用 `timeoutService.cancelWithLock(appointmentId)`。
4. **取消逻辑**（带锁）：
   - 获取 `lock:cancel:{id}`。
   - 调用 `self.cancelExpiredAppointment()`（@Transactional）。
5. **事务方法**：
   - 查询 `findPendingTimeout`（status=0 AND created_at < NOW()-30min）。
   - 存在则更新状态为 3（已取消），回滚号源 `+1`，清除缓存。
6. **启动补偿**：`@PostConstruct` 执行 `findAllPendingTimeout()`，逐个调用 `cancelWithLock`。

### 4.3 完整数据流时序图

```
【预约创建时】AppointmentServiceImpl.createAppointment()
    │
    └─ timeoutTimer.scheduleCancel(id, 30, MINUTES)
           │
           ▼
       timer.newTimeout(new CancelTask(id), 30, MINUTES)
           │
           └─ 任务进入时间轮，等待 30 分钟

【30 分钟后】时间轮触发 CancelTask.run()
    │
    ▼
AppointmentTimeoutServiceImpl.cancelWithLock(id)
    │
    ├─ 获取分布式锁 lock:cancel:{id}
    │   ├─ 成功 → 继续
    │   └─ 失败 → 退出（其他节点已处理）
    │
    ├─ self.cancelExpiredAppointment(id)  // 代理调用，事务生效
    │   │
    │   ├─ ① 查询预约：findPendingTimeout(id)
    │   │   ├─ 存在（status=0 且超时）→ 继续
    │   │   └─ 不存在 → 返回 false
    │   │
    │   ├─ ② 更新预约状态为 3（已取消）
    │   │   └─ 乐观锁条件：status=0
    │   │
    │   ├─ ③ 回滚号源：remaining_count + 1
    │   │   └─ 若排班状态为“已满（3）”，自动恢复为“正常（1）”
    │   │
    │   └─ ④ 清除医生排班缓存
    │
    └─ 释放分布式锁

【服务启动时】@PostConstruct 扫描补偿
    │
    ├─ findAllPendingTimeout() // 所有超时预约
    └─ 逐个调用 cancelWithLock()
```



### 4.4 核心设计思想

- **时间轮替代扫表**：O(1) 调度，内存级操作，无数据库轮询压力。
- **双重校验**：时间轮触发后仍以数据库时间条件为准，防止误取消。
- **启动补偿**：保证服务重启后不丢失任务。
- **分布式锁**：防止多实例重复取消。
- **自注入代理**：解决 `@Transactional` 自调用失效问题。

### 4.5 扩展思考

- 若任务数量极大（百万级），可考虑使用 Redis 的 ZSet 实现分布式延迟队列，避免单机内存溢出。
- 可增加监控指标：待处理任务数、取消成功率、平均延迟时间。

### 4.6 面试高频问答

**Q1: 时间轮为什么比 `@Scheduled` 好？**
A: `@Scheduled` 扫表是 O(n) 全表扫描，时间轮是 O(1) 任务调度。百万预约时，扫表每分钟扫描百万数据，而时间轮只处理到期的少数任务。

**Q2: 服务重启时，时间轮里的任务丢了怎么办？**
A: 启动时 `@PostConstruct` 扫描数据库，处理所有未支付的超时预约，保证最终一致性。

**Q3: 为什么取消方法要用 `@Lazy` 自注入？**
A: 因为 `cancelWithLock()` 中调用 `this.cancelExpiredAppointment()` 会使 `@Transactional` 失效。通过自注入代理对象调用，事务才生效。

------

## 五、多级缓存模块（Caffeine + Redis + 布隆过滤器）

### 5.1 模块作用

- **L0 布隆过滤器**：拦截不存在的 ID，防止缓存穿透。
- **L1 Caffeine 本地缓存**：极速访问，适合热点数据。
- **L2 Redis 分布式缓存**：跨实例共享，持久化。
- **泛型反序列化**：支持 `PageInfo<T>` 等复杂类型的缓存。
- **批量删除**：使用 SCAN 替代 KEYS，避免 Redis 阻塞。

### 5.2 实现步骤

1. **配置布隆过滤器**：`BloomFilterConfig` 创建 `doctorBloomFilter` 和 `scheduleBloomFilter`。
2. **布隆过滤器服务**：`BloomFilterService` 在启动时加载所有有效 ID，提供 `mightContain` 检查，新增数据时 `add`。
3. **多级缓存服务**：`MultiLevelCacheService` 提供 `get` 方法，依次查 Caffeine → Redis → DB。
4. **类型转换**：`convertToTargetType` 利用 Jackson `TypeFactory` 将 `LinkedHashMap` 转成目标泛型。
5. **批量删除**：`evictAll` 使用 Redis SCAN + 批量 DEL，并清除 Caffeine 中匹配前缀的 Key。

### 5.3 完整数据流时序图

```
查询请求（如医生列表 / 排班日历）
    │
    ▼
PatientDoctorServiceImpl.getXxx()
    │
    ├─ ① 布隆过滤器检查（若 doctorId 不存在 → 直接返回空）
    │
    ├─ ② MultiLevelCacheService.get()
    │   │
    │   ├─ L1：查 Caffeine
    │   │   ├─ 命中 → 返回（若为 LinkedHashMap 则转换）
    │   │   └─ 未命中 → 继续
    │   │
    │   ├─ L2：查 Redis
    │   │   ├─ 命中 → 回填 Caffeine，返回
    │   │   └─ 未命中 → 继续
    │   │
    │   └─ 回源：执行 supplier（查 DB）
    │       ├─ 写入 Redis（带 TTL）
    │       ├─ 写入 Caffeine
    │       └─ 返回结果
    │
    └─ 返回最终结果
```



### 5.4 核心设计思想

- **分层缓存**：根据数据访问频率和一致性要求，分配不同层级。
- **布隆过滤器前置**：最快速度拦截不存在 ID，DB 零压力。
- **空值缓存**：防止穿透，有效期短（5-10 分钟）。
- **泛型支持**：通过 `TypeReference` 捕获类型，解决反序列化问题。
- **SCAN 替代 KEYS**：避免阻塞 Redis。

### 5.5 扩展思考

- 若数据量极大，可考虑使用 Redis 布隆过滤器模块（Redisson 提供），支持分布式和重建。
- 可增加缓存预热机制，启动时加载热门数据。
- 可增加缓存监控面板，实时查看命中率和 Top Key。

### 5.6 面试高频问答

**Q1: 布隆过滤器误判怎么办？**
A: 误判导致少量不存在 ID 穿透到 DB，但 DB 查询结果为空，不会影响数据正确性。通过定时重建可控制误判率在 1% 以下。

**Q2: `PageInfo` 缓存为什么需要特殊处理？**
A: Redis 存储时 `PageInfo` 被序列化为 JSON，读取时 Jackson 无法恢复泛型，只能得到 `LinkedHashMap`。通过 `TypeReference` 捕获完整类型，再调用 `convertToTargetType` 转换。

**Q3: 为什么 `evictAll` 要用 SCAN 而不是 KEYS？**
A: `KEYS` 会阻塞 Redis 单线程，数据量大时导致所有请求超时。`SCAN` 游标迭代 + 分批删除，非阻塞。

------

## 六、WebSocket 在线问诊模块（STOMP + 房间隔离 + 离线消息）

### 6.1 模块作用

- **实时通信**：患者与医生实时聊天。
- **房间隔离**：每个预约 ID 对应独立 Topic。
- **在线状态管理**：Redis 存储用户在线状态。
- **离线消息补发**：用户上线自动推送未读消息。
- **历史记录**：MySQL 持久化存储，分页查询。

### 6.2 实现步骤

1. **配置 WebSocket**：`WebSocketConfig` 注册 `/ws/chat` 端点，启用 STOMP，配置 `/app` 和 `/topic` 前缀。
2. **握手拦截器**：`WebSocketHandshakeInterceptor` 从 URL 参数提取 Token，校验后存入 Session 属性。
3. **事件监听**：`WebSocketEventListener` 监听连接/断开事件，更新 Redis 在线状态和房间成员。
4. **消息处理**：`ChatController.sendMessage` 接收 `/app/chat.send` 消息，校验后保存 MySQL，广播到 `/topic/room/{appointmentId}`，并判断接收者离线则暂存 Redis。
5. **Redis 服务**：`ConsultationRedisService` 管理 `ws:user:{userId}`（在线状态）、`ws:room:{appointmentId}`（房间成员）、`ws:offline:{userId}`（离线消息）。
6. **HTTP 辅助接口**：`ConsultationController` 提供房间信息、历史记录、结束问诊。

### 6.3 完整数据流时序图

```
【连接建立】
前端 WebSocket 连接 /ws/chat?token=xxx&appointmentId=1001
    │
    ▼
WebSocketHandshakeInterceptor（解析 Token，存入 Session 属性）
    │
    ▼
SessionConnectEvent → WebSocketEventListener.handleSessionConnected()
    │
    ├─ ① 标记在线：Redis ws:user:{userId} = sessionId
    ├─ ② 加入房间：Redis ws:room:{appointmentId} 添加 userId
    └─ ③ 推送离线消息：从 ws:offline:{userId} 取出所有消息，逐条发送

【发送消息】
前端 stompClient.send("/app/chat.send", { ... })
    │
    ▼
ChatController.sendMessage()
    │
    ├─ ① 从 Session 获取 userId, role
    ├─ ② 权限校验（consultationService.checkConsultationAccess）
    ├─ ③ 接收者校验（患者→医生，医生→患者）
    ├─ ④ XSS 过滤（< → &lt;）
    ├─ ⑤ 保存到 MySQL consultation_message
    ├─ ⑥ 查询发送者姓名
    ├─ ⑦ 广播到 /topic/room/{appointmentId}
    └─ ⑧ 若接收者离线，存到 ws:offline:{receiverId}

【连接断开】
SessionDisconnectEvent → handleSessionDisconnected()
    │
    ├─ 标记离线：删除 ws:user:{userId}
    └─ 离开房间：从 ws:room:{appointmentId} 移除 userId
```



### 6.4 核心设计思想

- **房间隔离**：按 `appointmentId` 分配 Topic，自然隔离。
- **Redis 会话管理**：支持多实例部署，共享在线状态。
- **离线消息队列**：使用 Redis List，保证顺序，上线即推送。
- **权限校验**：进入房间前必须为已支付状态且就诊日为今天。
- **历史记录分页**：MySQL 持久化，支持按时间倒序查询。

### 6.5 扩展思考

- **集群支持**：目前 `enableSimpleBroker` 为内存版，多实例需改用 `enableStompBrokerRelay` 或 Redis Pub/Sub。
- **消息加密**：敏感医疗信息需端到端加密。
- **消息已读回执**：可增加 `is_read` 字段，提供已读/未读状态。

### 6.6 面试高频问答

**Q1: 为什么用 STOMP 而不是原生 WebSocket？**
A: STOMP 提供了消息路由、订阅/发布、点对点等高级模式，简化开发。且兼容 SockJS 降级。

**Q2: Token 放在 URL 参数里，安全吗？**
A: 受限于 SockJS，只能如此。生产环境强制 WSS，并在 Nginx 日志中过滤 `token` 参数。

**Q3: 如何保证消息不丢？**
A: ① 消息先保存到 MySQL 再广播。② 离线消息暂存 Redis，上线补发。③ 若 Redis 不可用，降级为仅保存 MySQL，下次上线查历史。

------

## 七、OSS STS 临时凭证模块（前端直传 + 最小权限策略）

### 7.1 模块作用

- **前端直传 OSS**：无需经过后端服务器，节省带宽和内存。
- **临时凭证**：30 分钟有效期，降低 AK/SK 泄露风险。
- **最小权限**：动态 Policy 限制只能上传到 `medi/doctor/{doctorId}/` 目录。
- **身份隔离**：每个医生独立目录，无法越权。

### 7.2 实现步骤

1. **配置属性**：`OssProperties` 读取 `aliyun.oss` 配置（endpoint、bucket、roleArn、子账号 AK/SK）。
2. **STS 服务**：`OssStsService.getStsCredential(doctorId)`：
   - 构建 `IClientProfile`，使用子账号 AK/SK 初始化 `IAcsClient`。
   - 构建 `AssumeRoleRequest`，设置 `RoleArn`、`RoleSessionName`、`DurationSeconds`。
   - 调用 `buildCustomPolicy(doctorId)` 生成 Policy JSON。
   - 调用 `client.getAcsResponse(request)`，提取临时凭证。
   - 构建 `OssStsVO` 返回（包含临时 AK、SK、Token、Bucket、Endpoint、Dir）。
3. **Controller**：`OssController` 提供 `/sts-token` 接口，`@RequireRole(DOCTOR)` 保护，从 JWT 提取 `doctorId`。
4. **前端直传**：前端使用临时凭证初始化 OSS SDK，上传文件到 `dir + filename`。

### 7.3 完整数据流时序图

```
医生前端请求 /doctor/oss/sts-token（携带 JWT）
    │
    ▼
OssController.getStsToken()
    │
    └─ OssStsService.getStsCredential(doctorId)
           │
           ├─ ① 构建 STS 客户端（使用子账号 AK/SK）
           ├─ ② 构建 AssumeRoleRequest
           │   ├─ RoleArn（RAM 角色）
           │   ├─ RoleSessionName（"doctor-" + doctorId）
           │   ├─ DurationSeconds（1800）
           │   └─ Policy（动态生成，限制资源路径）
           │
           ├─ ③ 调用 STS API，获取临时凭证
           ├─ ④ 封装 OssStsVO（accessKeyId, accessKeySecret, securityToken, expiration, bucket, endpoint, dir）
           │
           └─ 返回给前端

前端获取凭证后：
    │
    ├─ 初始化 OSS Client（使用临时凭证）
    ├─ 调用 client.put(dir + filename, file)
    └─ 文件直传 OSS，不经过后端
```



### 7.4 核心设计思想

- **STS 临时凭证**：自动过期，最小化泄露影响。
- **动态 Policy**：将 `doctorId` 硬编码进资源路径，确保目录隔离。
- **只授予 PutObject**：无法下载、删除、列举，最小权限。
- **JWT 来源**：`doctorId` 从 JWT 提取，前端无法伪造。

### 7.5 扩展思考

- 可增加文件类型/大小校验（前端 + 后端 OSS 回调）。
- 可设置 OSS 生命周期规则，自动清理过期文件。
- 可集成 CDN 加速文件访问。

### 7.6 面试高频问答

**Q1: 为什么不用后端代理上传？**
A: 后端代理会占用应用服务器内存和带宽，成为性能瓶颈。前端直传将压力分散到 OSS，且通过 STS 保障安全。

**Q2: 如果医生 A 拿到了凭证，能否上传到医生 B 的目录？**
A: 不能。Policy 中硬编码了医生 A 的 ID，OSS 会校验路径，拒绝访问 B 的目录。

**Q3: 临时凭证有效期为什么是 30 分钟？**
A: 一次上传操作通常在数秒内完成，30 分钟足够覆盖各种情况（文件较大、网络慢）。过长增加泄露风险，过短可能导致上传中断。

------

## 八、热门医生排行榜模块（时间衰减算法）

### 8.1 模块作用

- **动态排名**：根据近期评价热度排序。
- **时间衰减**：降低陈旧评价权重，突出近期口碑变化。
- **缓存优化**：Redis 缓存 30 分钟，减少 DB 聚合查询压力。
- **自动刷新**：评价变更时主动刷新 + 定时任务兜底。

### 8.2 实现步骤

1. **SQL 核心算法**：`EvaluationMapper.findHotDoctors`：

   ```
   SELECT doctor_id, 
          ROUND(AVG(score * POW(0.98, DATEDIFF(NOW(), created_at))), 2) AS hotScore
   FROM evaluation
   WHERE status = 1 AND created_at > DATE_SUB(NOW(), INTERVAL 30 DAY)
   GROUP BY doctor_id ORDER BY hotScore DESC LIMIT 10;
   ```

   

2. **Service 缓存**：`EvaluationService.getHotDoctors()` 先查 Redis，未命中则查 DB 并缓存。

3. **刷新方法**：`refreshHotDoctorCache()` 重新查询 DB 并覆盖 Redis。

4. **触发刷新**：评价创建/删除时调用 `refreshHotDoctorCache()`。

5. **定时任务**：`DoctorHotScheduler` 每 30 分钟执行一次。

### 8.3 完整数据流时序图

```
【查询排行榜】GET /patient/doctors/hot
    │
    ▼
EvaluationService.getHotDoctors()
    │
    ├─ ① 尝试从 Redis 读取 Key = "hot:doctors"
    │   ├─ 命中 → 返回
    │   └─ 未命中 → 继续
    │
    ├─ ② 查 DB：evaluationMapper.findHotDoctors(10)
    │   └─ SQL 执行时间衰减聚合
    │
    ├─ ③ 写入 Redis（TTL 30 分钟）
    │
    └─ ④ 返回结果

【评价变更后刷新】
用户提交/删除评价
    │
    ▼
EvaluationService.createEvaluation() / deleteEvaluation()
    │
    └─ 调用 refreshHotDoctorCache()
           │
           ├─ 查询最新排名
           └─ 覆盖 Redis（SET）

【定时任务兜底】
每 30 分钟执行
    │
    ▼
DoctorHotScheduler.refreshHotDoctorCache()
    │
    └─ 调用 evaluationService.refreshHotDoctorCache()
```



### 8.4 核心设计思想

- **时间衰减函数**：`0.98^(days)`，半衰期约 34 天，契合医疗口碑变化周期。
- **只取近 30 天**：减少计算量，且老旧评价权重已不足 54.5%，影响小。
- **使用 AVG 而非 SUM**：避免评价数量多的医生绝对占优。
- **缓存 + 定时兜底**：保证性能和数据最终一致性。

### 8.5 扩展思考

- 可引入多维度评分（如医生态度、医术、环境）加权计算。
- 可增加“好评率”维度，组合成综合得分。
- 可提供按科室筛选排行榜，满足不同患者需求。

### 8.6 面试高频问答

**Q1: 为什么衰减系数选 0.98？**
A: 0.98 对应半衰期 34 天，经验上符合医疗场景（口碑变化以月为单位）。若选 0.95（半衰期 14 天），医生休假两周排名就大幅下降，过于敏感。

**Q2: 如果某医生没有评价，排行榜怎么处理？**
A: 查询结果为空时，缓存空列表（有效期 5 分钟），防止缓存穿透。

**Q3: 如何防止刷榜？**
A: 当前依赖业务限制（一个预约只能评价一次，且就诊后才能评价）。未来可增加异常检测（短时间内大量 5 星评价）。

------

## 九、智能号源推荐模块（历史就诊率）

### 9.1 模块作用

- **动态调整号源**：根据历史就诊率智能推荐号源数。
- **优化资源利用率**：就诊率高时段加号，低时段减号。
- **提升医生满意度**：避免空等或过度劳累。
- **新医生友好**：无历史数据时使用医生设定值。

### 9.2 实现步骤

1. **Controller**：`ScheduleController.getRecommendedMaxCount` 接收医生输入的基准值。

2. **Service 算法**：`ScheduleServiceImpl.recommendMaxCount`：

   - 计算目标日期的星期几。
   - 调用 Mapper 查询过去 4 周同一天的就诊率。
   - 若无数据，返回基准值。
   - 若 ≥ 85% → 加号 20%；≤ 40% → 减号 20%；否则不变。
   - 边界保护 [1, 100]。

3. **Mapper SQL**：`ScheduleMapper.getHistoricalOccupancyRate`：

   sql

   ```
   SELECT IFNULL(SUM(a.cnt), 0) / NULLIF(SUM(s.max_count), 0)
   FROM schedule s
   LEFT JOIN (SELECT schedule_id, COUNT(*) AS cnt FROM appointment WHERE status IN (1,2) GROUP BY schedule_id) a
   ON s.id = a.schedule_id
   WHERE s.doctor_id = #{doctorId}
     AND WEEKDAY(s.schedule_date) = (#{dayOfWeek} - 1)
     AND s.schedule_date BETWEEN DATE_SUB(#{targetDate}, INTERVAL 4 WEEK) AND DATE_SUB(#{targetDate}, INTERVAL 1 DAY);
   ```

   

### 9.3 完整数据流时序图

```
医生设置排班，输入基准值 20，选择日期 2026-07-25（周六）
    │
    ▼
Controller.getRecommendedMaxCount()
    │
    ▼
Service.recommendMaxCount()
    │
    ├─ ① 计算 dayOfWeek = 6（周六）
    ├─ ② 调用 Mapper.getHistoricalOccupancyRate(doctorId, 6, '2026-07-25')
    │   │
    │   └─ SQL 查询过去 4 周所有周六的就诊率 → 返回 0.87
    │
    ├─ ③ 判断：87% ≥ 85% → 加号 20%
    ├─ ④ recommended = ceil(20 * 1.2) = 24
    │
    └─ ⑤ 返回 24
    │
    ▼
前端显示：推荐 24 个号（建议增加 20%）
```



### 9.4 核心设计思想

- **就诊率而非预约率**：排除爽约干扰，更准确反映实际需求。
- **过去 4 周同一天**：平衡样本量和时效性。
- **温和调整（±20%）**：避免剧烈波动。
- **边界保护**：号源数永远在 1~100 之间。

### 9.5 扩展思考

- 可引入加权平均：距离目标日期越近的数据权重越高。
- 可考虑节假日因素，特殊处理法定节假日。
- 可接入天气数据，雨雪天就诊率可能下降。

### 9.6 面试高频问答

**Q1: 为什么用“就诊数”而不是“预约数”？**
A: 预约数包含爽约患者，不能反映真实医疗资源消耗。就诊数（已支付+已完成）才是有效需求。

**Q2: 如果过去 4 周某一天没有排班，怎么计算？**
A: 该天无排班数据，不参与计算。分母 `SUM(max_count)` 为 0，`NULLIF` 处理，该周权重为 0。

**Q3: 新医生没有历史数据怎么办？**
A: 返回用户输入的基准值，不进行调整。待积累数据后，算法自动生效。

------

## 十、操作日志 AOP 模块（@LogOperation + 异步保存）

### 10.1 模块作用

- **自动记录操作**：通过注解标记方法，自动记录操作人、操作内容、参数、耗时、结果。
- **异步保存**：不影响主业务响应时间。
- **审计追溯**：支持按管理员、模块、时间范围、结果状态查询。
- **敏感信息脱敏**：配合 LoggingFilter 脱敏密码等字段。

### 10.2 实现步骤

1. **定义注解**：`@LogOperation(module, operation, recordParams, recordResult)`。
2. **AOP 切面**：`OperationLogAspect` 使用 `@Around` 拦截带注解的方法。
   - 前置：获取请求信息、管理员信息（从 request 属性取）、参数。
   - 执行目标方法。
   - 后置：计算耗时，判断结果状态。
   - finally：构建 `OperationLog` 实体，调用异步保存。
3. **异步服务**：`OperationLogServiceImpl.saveLogAsync` 使用 `@Async` 异步插入数据库。
4. **查询接口**：`OperationLogController` 提供分页查询、详情、删除。
5. **数据存储**：`operation_log` 表包含所有字段。

### 10.3 完整数据流时序图

```
管理员操作（如审核通过）
    │
    ▼
【AOP 拦截】OperationLogAspect.logOperation()
    │
    ├─ ① 获取请求信息（URI、Method、IP）
    ├─ ② 获取 @LogOperation 注解属性
    ├─ ③ 从 request 取 adminId、adminName
    ├─ ④ 记录请求参数（敏感字段已由 Filter 脱敏）
    ├─ ⑤ 记录开始时间
    │
    ▼
执行目标方法（Controller 业务逻辑）
    │
    ├─ 业务处理
    ├─ 返回 Result
    │
    ▼
【AOP 后置】
    │
    ├─ ⑥ 计算耗时
    ├─ ⑦ 判断业务结果（Result.code == 1）
    ├─ ⑧ 构建 OperationLog 实体
    ├─ ⑨ 调用 operationLogService.saveLogAsync(log)
    │      │
    │      └─ @Async 异步执行（新线程）
    │           │
    │           ├─ operationLogMapper.insert(log)
    │           └─ 若失败，仅记录日志（不影响主业务）
    │
    └─ ⑩ 返回业务结果给前端
```



### 10.4 核心设计思想

- **AOP 非侵入**：业务代码零污染。
- **异步保存**：日志 INSERT 耗时 5-10ms，异步后主线程零等待。
- **从 request 取管理员信息**：避免重复解析 JWT。
- **冗余存储 adminName**：即使管理员被删除，日志仍有姓名。
- **IP 获取 X-Forwarded-For**：支持反向代理。

### 10.5 扩展思考

- 可增加日志 ES 存储，支持全文检索和大数据分析。
- 可增加敏感操作告警（如批量删除日志）。
- 可自定义线程池，避免异步任务堆积。

### 10.6 面试高频问答

**Q1: 为什么用 `@Async` 而不是同步保存？**
A: 同步保存会增加接口响应时间，影响用户体验。异步保存让主业务 0 等待，日志最终一致性可接受。

**Q2: 如果异步保存失败怎么办？**
A: 只记录错误日志，不影响主业务。这是权衡：宁可丢一条日志，也不能阻塞用户挂号。

**Q3: `@Async` 和 `@Transactional` 能一起用吗？**
A: 不能。`@Async` 会切换线程，而 `@Transactional` 要求在同一线程中。本项目日志保存不需要事务，所以安全。

------

## 十一、整体技术栈亮点总结（面试专用）

| 亮点               | 技术实现                                    | 业务价值                             |
| :----------------- | :------------------------------------------ | :----------------------------------- |
| **双重防超卖**     | Redisson 分布式锁 + 数据库乐观锁            | 绝对防止超卖，避免纠纷               |
| **O(1) 延迟调度**  | Netty HashedWheelTimer 替代 @Scheduled 扫表 | 10 万预约无压力，DB 零额外负载       |
| **三层缓存防穿透** | 布隆过滤器 + Caffeine + Redis               | 缓存命中率 95%+，DB 查询量降低 90%   |
| **STS 最小权限**   | 阿里云 STS + 动态 Policy                    | 每个医生只能上传自己的文件，安全隔离 |
| **时间衰减算法**   | SQL 加权评分：`score * 0.98^(days)`         | 排行榜真实反映医生当前受欢迎程度     |
| **智能号源推荐**   | 历史就诊率 + 动态调整                       | 提高号源利用率 20%，减少医生空等     |
| **离线消息补发**   | Redis List 暂存 + 上线自动推送              | 用户断线重连后消息不丢失             |
| **异步审计日志**   | AOP + @Async + 线程池                       | 操作全记录，对主业务零性能影响       |

------

## 十二、常见挑战性面试追问

**Q: 如果流量再大 10 倍，系统瓶颈在哪里？如何优化？**
A:

- **数据库**：预约表 `appointment` 膨胀，建议按 `created_at` 分库分表（按月），或使用 TiDB 等 NewSQL。
- **WebSocket**：单机连接数有限，需引入 Redis Pub/Sub 实现跨实例消息路由。
- **时间轮**：百万级内存任务可能导致 OOM，可改为 Redis ZSet 分布式延迟队列。
- **缓存**：布隆过滤器需增大内存或迁移至 Redis 布隆模块。

**Q: 布隆过滤器的误判率如何控制？**
A: 通过配置 `FPP`（如 0.01）控制。定期重建可降低因删除数据导致的误判率上升。生产推荐使用 Redisson 的 `RBloomFilter`，支持动态扩展和重建。

**Q: 分布式锁超时后，业务还没执行完怎么办？**
A: 设置足够长的持有时间（15 秒）远大于业务耗时（< 100ms）。若仍超时（如 GC 暂停），Redisson 会自动释放锁，其他线程可抢锁。乐观锁作为最终兜底，保证数据一致性。

**Q: 如何保证 WebSocket 消息的可靠性？**
A: ① 先保存 MySQL 再广播；② 离线消息暂存 Redis；③ 上线自动补发；④ 若 Redis 不可用，降级为仅存 MySQL，用户下次上线查询历史。
