# MediReserve 智慧医疗平台 —— 完整开发日记

> 项目周期：2026-07-02 ~ 2026-07-26  
> 技术栈：Spring Boot 3.3.6 / JDK 17 / Maven 多模块 / MyBatis / MySQL / Redis / Redisson / JWT / BCrypt / PageHelper / Knife4j / Netty HashedWheelTimer / STOMP WebSocket / Docker  
> 开发人员：wzx  
> 本文档记录了从零搭建到项目交付的全部过程，包含每日任务、遇到的所有问题、解决方案及思路演变。

---

## 目录

- [第一阶段：项目初始化与环境搭建（07-02 ~ 07-03）](#第一阶段项目初始化与环境搭建0702--0703)
- [第二阶段：三端认证系统开发（07-03 ~ 07-05）](#第二阶段三端认证系统开发0703--0705)
- [第三阶段：排班管理模块开发（07-06 ~ 07-08）](#第三阶段排班管理模块开发0706--0708)
- [第四阶段：管理员审核医生模块开发（07-08）](#第四阶段管理员审核医生模块开发0708)
- [第五阶段：预约挂号模块开发（07-09 ~ 07-11）](#第五阶段预约挂号模块开发0709--0711)
- [第六阶段：号源展示模块开发（07-11 ~ 07-12）](#第六阶段号源展示模块开发0711--0712)
- [第七阶段：代码审查与全面优化（07-12 ~ 07-18）](#第七阶段代码审查与全面优化0712--0718)
- [第八阶段：测试与问题修复（07-18 ~ 07-23）](#第八阶段测试与问题修复0718--0723)
- [第九阶段：进阶功能开发（07-18 ~ 07-22）](#第九阶段进阶功能开发0718--0722)
- [第十阶段：收尾与部署准备（07-23 ~ 07-24）](#第十阶段收尾与部署准备0723--0724)
- [第十一阶段：文档与项目交付（07-24 ~ 07-26）](#第十一阶段文档与项目交付0724--0726)
- [附录：核心问题与解决方案速查表](#附录核心问题与解决方案速查表)

---

## 第一阶段：项目初始化与环境搭建（07-02 ~ 07-03）

### 目标
- 构建 Maven 多模块工程
- 设计数据库表结构
- 搭建基础架构（统一返回、全局异常、工具类）

### 具体工作

#### 1. 多模块项目创建
- 父工程 `medi-reserve`（pom 打包），管理依赖版本
- 子模块：`medi-common`（公共组件）、`medi-patient`（患者端）、`medi-doctor`（医生端）、`medi-admin`（管理端）、`medi-websocket`（WebSocket 服务）
- Spring Boot 3.3.6 + JDK 17

#### 2. 数据库设计
设计 14 张表，涵盖业务全流程：
- **用户认证**：`patient`、`doctor`、`admin`
- **字典**：`department`、`title`
- **核心业务**：`schedule`（排班）、`appointment`（预约）
- **审核扩展**：`doctor_audit`
- **评价与消息**：`evaluation`、`consultation_message`
- **权限审计**：`permission`、`role`、`role_permission`、`operation_log`

#### 3. 基础架构搭建
- `Result<T>` 统一返回格式（code/msg/data）
- `GlobalExceptionHandler` 统一处理业务异常、参数校验异常、系统异常
- `JwtUtil`（JJWT）和 `PasswordUtil`（BCrypt）
- 常量类：`MessageConstant`、`StatusCodeConstant`、`StatusConstant`、`RoleConstant`
- 自定义异常基类 `BusinessException`，派生 30+ 子类

### 遇到的问题与解决思路

**问题1：MySQL 驱动依赖报错**  
```
'dependencies.dependency.version' for mysql:mysql-connector-java:jar is missing
```
**原因**：Spring Boot 3.x 将 MySQL 驱动 artifactId 改为 `mysql-connector-j`（不再使用 `-java` 后缀）。  
**解决**：在父 POM 中引入 `com.mysql:mysql-connector-j`，scope 为 runtime。

**问题2：Knife4j 与 Spring Boot 3 不兼容**  
**原因**：使用了 `knife4j-spring-boot-starter`（基于 javax）。  
**解决**：改用 `knife4j-openapi3-jakarta-spring-boot-starter` 版本 4.5.0，适配 jakarta。

**问题3：IDEA 提示 YAML 配置 location 爆红**  
**原因**：IDEA 不认识 Knife4j 自定义属性 `knife4j.documents.location`。  
**解决**：确认配置正确，运行时无问题，忽略 IDE 误报。

### 阶段成果
- 五模块均可独立启动
- Knife4j 接口文档可访问
- 数据库结构完整，含初始数据（科室、职称、管理员、权限）

---

## 第二阶段：三端认证系统开发（07-03 ~ 07-05）

### 目标
- 实现 JWT 令牌生成与解析
- 实现三端登录注册
- 构建完整自定义异常体系
- 集成登录失败锁定机制（Redis）

### 具体工作

#### 1. JWT 认证体系
- `JwtUtil.createToken(userId, username, role)` 生成令牌
- `JwtTokenInterceptor` 拦截器：提取 Authorization Header，解析 Token，校验有效性，将 `userId`、`role` 存入 `request.setAttribute`，供 Controller 使用
- `@RequireRole` 自定义注解，在拦截器中校验角色

#### 2. 三端登录注册
- **患者端**：`/patient/register`（手机号注册，BCrypt 加密），`/patient/login` 返回 JWT
- **医生端**：`/doctor/register`（注册时自动创建 `doctor_audit` 记录，`audit_status=0`），登录时校验审核状态（待审核/驳回则拒绝）
- **管理端**：`/admin/register`（仅超级管理员可调用，强制角色为普通管理员），`/admin/login` 返回 JWT

#### 3. 登录失败锁定
- 使用 Redis 记录失败次数，Key = `login:fail:{username}`
- 失败 5 次后锁定 15 分钟，锁定期间登录直接抛出 `AccountLockedException`
- 登录成功清空失败计数

#### 4. 异常体系
- 创建 30+ 自定义异常类，如 `AccountNotFoundException`、`PasswordErrorException`、`PhoneAlreadyExistsException`、`AuditPendingException` 等
- 全局异常处理器区分类型，返回相应状态码和提示

### 遇到的问题与解决思路

**问题1：JWT 中字段名与拦截器读取不一致**  
**现象**：`@RequestAttribute("userId")` 取到 null，但 `role` 正常。  
**原因**：`JwtUtil.createToken()` 存入的 key 为 `"id"` 和 `"phone"`，而拦截器读取 `"userId"` 和 `"username"`。  
**解决**：统一为 `userId` 和 `username`，修改 `createToken` 中的 claims。

**问题2：管理端密码校验逻辑写反**  
```java
// 错误写法
if (PasswordUtil.matches(password, admin.getPassword())) {
    throw new PasswordErrorException();
}
// 正确写法
if (!PasswordUtil.matches(password, admin.getPassword())) {
    throw new PasswordErrorException();
}
```
**原因**：逻辑错误，笔误。  
**解决**：加 `!` 取反。

**问题3：未审核医生登录异常**  
**现象**：新注册医生登录时报 `AuditPendingException`，预期行为但未友好提示。  
**解决**：在 `DoctorAuthServiceImpl.login()` 中增加审核状态判断，分别抛出 `AuditPendingException`（待审核）和 `AuditRejectedException`（驳回），并给出明确提示。

### 阶段成果
- 三端认证全部打通
- JWT 拦截器稳定运行
- 异常体系覆盖全面

---

## 第三阶段：排班管理模块开发（07-06 ~ 07-08）

### 目标
- 医生排班 CRUD（增删改查、停诊/恢复）
- 智能号源推荐算法（基于历史就诊率）
- 权限控制（从 JWT 取医生 ID，校验排班归属）

### 具体工作

#### 1. 排班 CRUD
- **新增排班**：`POST /doctor/schedules`，防重校验（同一天同一时段），插入 `schedule` 记录，`remaining_count = max_count`
- **查询排班**：`GET /doctor/schedules`，支持按日期范围、状态筛选
- **停诊/恢复**：`PATCH /doctor/schedules/{id}/status`，状态 1=正常，2=停诊，校验号源是否已满（已满不可停诊）
- **删除排班**：`DELETE /doctor/schedules/{id}`，校验无预约记录方可删除

#### 2. 智能推荐算法
- 计算目标日期星期几
- 查询过去 4 周同一天的平均就诊率：`就诊率 = 实际就诊数 / 总号源数`
- 若就诊率 ≥ 85%，推荐值 = `ceil(基准值 * 1.2)`；≤ 40% 则 `ceil(基准值 * 0.8)`；否则不变
- 边界保护：最终值在 [1, 100]

#### 3. 权限控制
- 所有接口从 JWT 获取 `doctorId`，不再依赖前端传入
- 更新/删除/停诊时校验排班归属，非本人操作抛出 `PermissionDeniedException`

### 遇到的问题与解决思路

**问题1：MyBatis 多参数映射失败**  
**现象**：`countByDoctorDatePeriod` 方法报 SQL 参数异常。  
**原因**：多参数未使用 `@Param`。  
**解决**：所有 Mapper 方法明确使用 `@Param` 绑定参数。

**问题2：动态 SQL 在注解中难以维护**  
**原因**：`findByDoctorIdAndDateRange` 使用 `@Select("<script>...</script>")`，可读性差。  
**解决**：迁移到 `ScheduleMapper.xml`，使用 `<where>` 标签。

**问题3：删除排班时 `appointment` 表尚未创建**  
**解决**：先创建基础表结构，用于 `countAppointmentsByScheduleId` 查询。

**问题4：智能推荐算法使用模拟数据，未对接真实历史**  
**原因**：初期用 `simulateHistoricalOccupancyRate()` 模拟。  
**解决**：改为调用 `ScheduleMapper.getHistoricalOccupancyRate()`，查询 `appointment` 表真实数据。无历史数据时返回用户输入值。

**问题5：`listSchedule` 方法可能 NPE**  
```java
if (scheduleQueryDTO != null) {
    startDate = scheduleQueryDTO.getStartDate();
    // ...
}
```
**解决**：增加判空处理。

### 阶段成果
- 排班管理完整闭环
- 智能推荐算法基于真实数据运行
- 权限控制无越权风险

---

## 第四阶段：管理员审核医生模块开发（07-08）

### 目标
- 实现医生注册审核（待审核列表、通过、驳回）
- 记录审核操作人、时间、驳回原因
- 集成 PageHelper 分页

### 具体工作

#### 1. 审核接口
- `GET /admin/doctors/pending`：分页查询 `audit_status=0` 的医生，关联科室、职称
- `PATCH /admin/doctors/{id}/approve`：更新 `audit_status=1`，记录 `auditor_id`、`audit_time`
- `PATCH /admin/doctors/{id}/reject`：需传入 `rejectReason`，更新 `audit_status=2`，记录驳回原因和审核人

#### 2. 异常处理
- 新增 `DoctorAuditNotFoundException`、`AuditOperationFailedException`、`RejectReasonEmptyException`
- 审核前校验医生存在、账号状态、是否已审核

### 遇到的问题与解决思路

**问题1：`findPendingList()` 使用 `@Select` 导致 SQL 复杂**  
**解决**：迁移到 `AdminAuditMapper.xml`，使用联表查询。

**问题2：审核通过时日志信息与异常类型不匹配**  
```java
log.warn("审核通过失败，审核数据不存在，医生ID：{}", doctorId);
throw new DoctorAlreadyAuditedException();  // 应该是已审核异常
```
**解决**：修正日志描述，区分异常类型。

**问题3：未校验 `doctorAudit` 是否为 null**  
**解决**：在判状态前先判空。

### 阶段成果
- 医生审核完整流程（注册 → 待审核 → 通过/驳回）
- 操作审计可追溯

---

## 第五阶段：预约挂号模块开发（07-09 ~ 07-11）

### 目标
- 实现挂号下单（双重防超卖）
- 实现模拟支付（幂等）
- 实现超时自动取消（Netty 时间轮）

### 具体工作

#### 1. 挂号下单
- 前置校验：排班存在、日期未过、状态正常、号源未满、未重复预约
- **分布式锁**：`RedissonClient.getLock("lock:schedule:" + scheduleId)`，`tryLock(3, 15, SECONDS)`
- **乐观锁扣减**：`UPDATE schedule SET remaining_count = remaining_count - 1 WHERE id = ? AND remaining_count > 0`，检查影响行数
- 插入 `appointment` 记录，状态 `0-待支付`，生成单号 `APPOINTMENT_ + yyyyMMddHHmmss + 4位随机数`
- 清除该医生排班缓存（`patientDoctorService.clearScheduleCache(doctorId)`）
- 启动时间轮任务：`timeoutTimer.scheduleCancel(appointmentId, 30, MINUTES)`

#### 2. 模拟支付
- 校验预约存在、归属、状态（已支付直接返回成功）、超时（30 分钟外拒绝）
- **乐观锁更新**：`UPDATE appointment SET status = 1 WHERE id = ? AND status = 0`，检查影响行数

#### 3. 超时取消（时间轮）
- 使用 Netty `HashedWheelTimer`（tick=100ms，512 槽）
- 30 分钟后触发 `CancelTask`，调用 `AppointmentTimeoutService.cancelWithLock(appointmentId)`
- 取消逻辑（带分布式锁 `lock:cancel:{id}`）：
  - 查询 `findPendingTimeout`（status=0 AND created_at < NOW()-30min）
  - 若存在，更新状态为 `3-已取消`，回滚号源（`remaining_count+1`，若排班状态为已满则恢复为正常）
  - 清除医生排班缓存
- 启动扫描：`@PostConstruct` 执行 `findAllPendingTimeout()`，逐个调用 `cancelWithLock`，补偿服务重启丢失的任务

### 遇到的问题与解决思路

**问题1：Netty 版本冲突导致 `NoSuchMethodError`**  
**现象**：`java.lang.NoSuchMethodError: 'java.util.Queue io.netty.util.internal.PlatformDependent.newFixedMpscUnpaddedQueue(int)'`  
**原因**：Redisson 3.27.2 依赖 Netty 4.1.115，但 Spring Boot 默认引入 4.1.107。  
**解决**：父 POM 中引入 `netty-bom` 统一版本为 4.1.115.Final。

**问题2：`String.format("$04d", ...)` 格式错误**  
**原因**：Java 格式化必须以 `%` 开头。  
**解决**：改为 `String.format("%04d", random)`。

**问题3：支付接口缺少乐观锁条件**  
```java
// 修改前：无条件更新，存在并发风险
@Update("UPDATE appointment SET status = #{status} WHERE id = #{id}")
// 修改后：条件更新，只有状态为0时才更新
@Update("UPDATE appointment SET status = #{status} WHERE id = #{id} AND status = 0")
```

**问题4：超时取消的 `@Transactional` 不生效**  
**原因**：`CancelTask` 是 `AppointmentTimeoutTimer` 的内部类，通过 `this.cancelExpiredAppointment()` 自调用，Spring AOP 无法代理。  
**解决**：将取消逻辑抽离到独立的 `AppointmentTimeoutService`，在 `cancelWithLock` 中通过 `@Lazy` 注入自身代理，调用 `self.cancelExpiredAppointment()`（代理调用，事务生效）。

**问题5：超时取消后未清除缓存**  
**解决**：在 `cancelExpiredAppointment` 中调用 `patientDoctorService.clearScheduleCache(doctorId)`。

**问题6：启动扫描超时预约无分布式锁**  
**解决**：`@PostConstruct` 中调用 `cancelWithLock()`，避免多实例重复执行。

**问题7：预约不存在时错用 `AccountNotFoundException`**  
**解决**：新建 `AppointmentNotFoundException`，专用于预约场景。

### 阶段成果
- 挂号下单双锁防超卖
- 支付幂等处理
- 超时取消使用时间轮 + 启动扫描兜底，保证最终一致性

---

## 第六阶段：号源展示模块开发（07-11 ~ 07-12）

### 目标
- 患者端查询科室、医生、排班日历
- 集成缓存策略（Caffeine + Redis + 布隆过滤器）

### 具体工作

#### 1. 号源展示接口
- `GET /patient/departments`：科室列表（含医生数量），缓存 1 小时
- `GET /patient/doctors`：分页医生列表，支持科室筛选、关键词搜索，缓存 5 分钟
- `GET /patient/doctors/{id}/schedules`：未来 7 天排班日历，缓存 1 分钟

#### 2. 缓存策略
- 科室、职称使用 Spring `@Cacheable`，Redis 存储
- 医生列表、排班日历使用 `MultiLevelCacheService`（Caffeine L1 + Redis L2 + 布隆过滤器防穿透）
- 布隆过滤器（Guava）初始化时加载所有有效医生 ID，拦截不存在的 ID

### 遇到的问题与解决思路

**问题1：动态 SQL 在注解中难以维护**  
**解决**：将 `PatientDoctorMapper` 和 `PatientScheduleMapper` 的 SQL 迁移到 XML。

**问题2：`CommonDictController` 直接调用 Mapper 绕过缓存**  
**解决**：改为注入 `PatientDoctorService`，调用有缓存的方法。

**问题3：`PageInfo` 缓存后反序列化失败**  
**现象**：`java.lang.ClassCastException: LinkedHashMap cannot be cast to PageInfo`  
**原因**：Jackson 无法还原泛型。  
**解决**：移除 `@Cacheable`，改用 `MultiLevelCacheService` 配合 `TypeReference` 传递泛型类型，在 `convertToTargetType` 中利用 `TypeFactory` 精准转换。

**问题4：`t.weight` 字段不存在**  
**现象**：`Unknown column 't.weight' in 'field list'`  
**原因**：`title` 表实际字段名为 `sort_order`。  
**解决**：SQL 中改为 `t.sort_order AS titleWeight`。

**问题5：GET 请求通过 Body 传参导致接收失败**  
**原因**：GET 请求参数应在 URL Query String。  
**解决**：在 Swagger 调试时直接拼接参数，如 `?department=内科&page=1&size=10`。

### 阶段成果
- 患者端号源展示完整
- 多级缓存 + 布隆过滤器显著降低 DB 压力

---

## 第七阶段：代码审查与全面优化（07-12 ~ 07-18）

在核心业务完成后，我进行了深入的系统级审查，发现并修复了 20+ 潜在问题。

### 主要优化点

#### 1. 权限控制补充
- `CommonDictController` 原本考虑加 `@RequireRole`，但最终决定字典接口公开，无需登录，只保留 Swagger 注释。
- 所有需要登录的接口统一加 `@RequireRole` 或类级注解，确保拦截器生效。

#### 2. 硬编码消息提取
- `MessageConstant` 补充 30+ 条提示消息（如 `DOCTOR_AUDIT_APPROVE_SUCCESS`、`SCHEDULE_DUPLICATE` 等）
- Controller 和 Service 中所有字符串替换为常量引用。

#### 3. 异常体系完善
- 新建 5 个异常类：`PasswordConfirmException`、`PasswordSameException`、`SelfDisableException`、`NoCertificatePendingException`、`InvalidAuditResultException`
- 统一使用 `StatusCodeConstant` 中的状态码。

#### 4. 角色常量统一
- `RoleConstant.NORMAL_ADMIN` 改为 `ADMIN`，与 `JwtTokenInterceptor` 中的判断一致，避免普通管理员权限校验失败。

#### 5. 实体类与数据库字段对齐
- `Patient.java` 移除 `birthDate` 和 `avatar` 字段（数据库不存在），注释掉并添加说明。

#### 6. OSS STS 路径兼容
- `OssController` 同时支持 `/sts-token` 和 `/sts` 两个路径，兼容不同前端版本。

#### 7. 分布式锁超时优化
- 锁等待时间保持 3 秒，持有时间从 10 秒提升到 15 秒，避免复杂场景（如网络抖动）下锁提前释放。

#### 8. 缓存空值保护增强
- `MultiLevelCacheService` 中空值缓存 TTL 从 5 分钟延长至 10 分钟，减少频繁穿透。

#### 9. WebSocket 消息优化
- 移除实时消息中的 `isSelf` 字段（写死为 false），前端自行根据 `senderId` 比对。
- 点对点推送改为仅当接收者离线时执行，避免在线时重复接收广播和点对点两条消息。

#### 10. 全局异常处理增强
- 新增捕获 `RedisTimeoutException`、`RedisException`、`InterruptedException` 等，返回 `SYSTEM_BUSY` 或 `SYSTEM_ERROR`。
- 所有 `BusinessException` 子类使用 `@Getter` 公开 `code` 字段。

#### 11. 请求日志过滤器
- 新增 `LoggingFilter`，记录每个 HTTP 请求的 URI、方法、耗时、状态码。
- 敏感字段（密码、旧密码等）自动脱敏（正则替换）。

#### 12. Docker 多环境配置
- 编写 `Dockerfile`（多阶段构建，maven → JRE alpine）
- 编写 `docker-compose.yml` 编排 MySQL、Redis、4 个微服务、Nginx
- 支持 `application-docker.yml` Profile，连接服务名 `mysql`/`redis`
- 环境变量全部从 `.env` 文件注入。

### 阶段成果
- 代码质量大幅提升，消除多处隐患
- Docker 部署方案落地，一键启动

---

## 第八阶段：测试与问题修复（07-18 ~ 07-23）

通过全面测试，发现了 15 个实际运行中的问题，逐一修复。

### 问题列表及解决方案

| #    | 问题现象                                       | 原因                                                         | 解决方案                                                     |
| ---- | ---------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 1    | Jackson 序列化 LocalDateTime 失败              | `GenericJackson2JsonRedisSerializer` 未注册 `JavaTimeModule` | 在 `CacheConfig` 中为 `ObjectMapper` 注册 `JavaTimeModule`，禁用 `WRITE_DATES_AS_TIMESTAMPS` |
| 2    | 部门列表排序错误                               | SQL 中 `ORDER BY t.sort_order DESC`，但 `sort_order` 值越小权重越高，导致主任医师排最后 | 调整数据库 `sort_order` 值（主任=4，副主任=3，主治=2，住院=1），排序方向保持 DESC |
| 3    | 未审核医生访问排班返回空而非 404               | `getScheduleCalendar()` 只校验医生存在，未校验审核状态       | 增加审核状态校验，未审核抛出 `DoctorNotFoundException`       |
| 4    | 参数名拼写错误 `patiendId` → `patientId`       | SQL 中参数名写错，导致重复预约校验失效                       | 修正所有 SQL 中的参数名                                      |
| 5    | `incrementRemainingCount` 强制将状态设为 1     | 回滚号源时无条件 `status = 1`，可能覆盖停诊状态              | 使用 `CASE WHEN`，仅当原状态为 3（已满）时才恢复为 1         |
| 6    | `decrementRemainingCount` 可能覆盖停诊状态     | 扣减时未保留原有状态                                         | 同样使用 `CASE WHEN`，只在 `remaining_count-1=0` 时设状态为 3，否则保留原状态 |
| 7    | 管理员角色名不一致导致 403                     | `RoleConstant` 中 `ADMIN_NORMAL`，但拦截器只识别 `ADMIN`     | 统一为 `ADMIN`，并修改 `getRoleName` 返回 `ADMIN`            |
| 8    | `AdminAuditController.getAuditDetail` 越权漏洞 | 未加 `@RequireRole`，任何人可查看任意医生审核详情            | 添加 `@RequireRole(RoleConstant.SUPER_ADMIN)`                |
| 9    | `Patient` 实体类包含数据库不存在的字段         | 实体类有 `birthDate`/`avatar`，但表无列                      | 注释掉这两个字段，并在文档中说明                             |
| 10   | OSS STS 路径前端请求 404                       | 前端请求 `/doctor/oss/sts`，后端只映射 `/sts-token`          | 增加 `@GetMapping({"/sts-token", "/sts"})` 双路径            |
| 11   | `findCertPendingList` 无 LIMIT，导致全表扫描   | XML 中未加 LIMIT，Service 层若未分页则 OOM                   | Service 层使用 `PageHelper.startPage()` 自动添加 LIMIT，XML 中不加手动 LIMIT |
| 12   | `findFutureScheduleIds` 存在无用参数           | 方法签名中有 `@Param("startDate")` 但 SQL 未使用             | 移除无用参数                                                 |
| 13   | `CommonDictController.getTitles()` 缺少日志    | 不利于排查                                                   | 补充 `log.info("获取职称列表")`                              |
| 14   | Knife4j 静态资源硬编码版本号                   | 在 `ResourceConfig` 中硬编码 `/4.5.0/`，升级版本需改多处     | 将版本号提取为常量，或直接删除映射（Knife4j starter 已自动配置） |
| 15   | DTO/VO 缺少 `@Schema` 注解                     | Knife4j 文档字段无描述                                       | 为所有 DTO/VO 类添加 `@Schema` 注解，描述字段含义            |

---

## 第九阶段：进阶功能开发（07-18 ~ 07-22）

在修复测试问题的同时，并行开发了几个重要进阶功能。

### 1. 操作日志（AOP 实现）
- 自定义注解 `@LogOperation(module, operation, recordParams, recordResult)`
- AOP 切面 `OperationLogAspect`：
  - 环绕通知，拦截带注解的方法
  - 从 Request 属性获取当前管理员信息
  - 记录请求参数（过滤 HttpServletRequest/Response）
  - 执行目标方法，记录耗时和结果
  - 异步保存日志（`@Async`）
- 日志实体 `OperationLog`，包含 adminId、adminName、module、operation、method、path、params、ip、result、statusCode、durationMs
- 查询接口：`/admin/operation-logs` 支持分页、按管理员/模块/时间/结果筛选

### 2. 权限细化（RBAC）
- 设计 `permission`、`role`、`role_permission` 表
- 初始化 13 条权限，超级管理员拥有全部，普通管理员拥有查看类权限
- 自定义 `@RequirePermission("admin:audit:approve")` 注解
- 拦截器中查询数据库校验权限码
- 更新角色权限时使用 `@CacheEvict` 清除权限缓存

### 3. 医生个人信息编辑
- `PUT /doctor/profile`：普通信息（姓名、手机号、性别、身份证）立即生效
- 证件信息（执业证书、资格证）提交审核：存入 `pending_certificate_url` 等字段，`cert_audit_status=0`
- 管理员审核通过/驳回：`approveCert` / `rejectCert` 方法，通过则 pending 覆盖正式字段，驳回则清空 pending
- 医生端可查询审核状态：`GET /doctor/profile/audit-status`

### 4. 医生端数据统计
- `GET /doctor/statistics/overview`：总接诊数、好评率、平均评分、今日接诊数、待处理问诊数
- `GET /doctor/statistics/trend`：近 N 天每日接诊量
- `GET /doctor/statistics/evaluations`：分页评价列表，匿名显示“匿名用户”

### 遇到的问题
- `CacheEvictService` 原本在 `medi-patient` 中，`AdminAuditServiceImpl` 需要清除缓存，导致模块依赖问题。解决：将缓存清除接口抽象到 `medi-common`，各模块实现具体逻辑。

---

## 第十阶段：收尾与部署准备（07-23 ~ 07-24）

### 主要工作
1. **全局异常处理优化**：增加更多 Redis 相关异常捕获，返回友好错误。
2. **缓存空值保护增强**：延长空值缓存 TTL 至 10 分钟。
3. **统一响应时间记录**：`LoggingFilter` 已实现，完善敏感字段脱敏。
4. **Docker 多环境配置**：编写完整 `docker-compose.yml`，包含 Nginx 反向代理，支持 4 个后端服务 + MySQL + Redis。
5. **项目全景分析报告**：使用工具扫描全部 155 个 Java 文件，生成 `PROJECT_OVERVIEW.md`，包含结构树、类注释、模块依赖图、技术栈清单。

### 关键决策
- 决定不采用分库分表（当前数据量可支撑），但预留扩展方案。
- 决定将 WebSocket 集群支持列为低优先级，当前单机已满足业务。

---

## 第十一阶段：文档与项目交付（07-24 ~ 07-26）

### 交付物清单
| 文档                   | 内容                                                         |
| ---------------------- | ------------------------------------------------------------ |
| `API_DOCUMENTATION.md` | Knife4j 导出的 OpenAPI 3.0 JSON/YAML 及说明                  |
| `DEPLOY.md`            | 环境要求、配置说明、数据库初始化、Docker 部署、手动部署、常见问题 |
| `USER_MANUAL.md`       | 患者端/医生端/管理端完整操作指南                             |
| `ER_DIAGRAM.md`        | 14 张表结构 + Mermaid 关系图                                 |
| `ARCHITECTURE.md`      | 整体架构、技术栈、缓存架构、WebSocket 消息流、安全架构       |
| `PROJECT_OVERVIEW.md`  | 全量代码扫描分析，包含包/类职责、模块依赖                    |
| `DEVELOPMENT_DIARY.md` | 本文档                                                       |

### 最终代码结构
```
medi-reserve-backend/
├── medi-common/          # 公共模块（实体、DTO、常量、工具、配置、拦截器、Service）
├── medi-patient/         # 患者端（控制器、Service、Mapper、定时器）
├── medi-doctor/          # 医生端（控制器、Service、Mapper）
├── medi-admin/           # 管理端（控制器、Service、Mapper、AOP切面）
├── medi-websocket/       # WebSocket 服务（STOMP 配置、控制器、Service、监听器）
└── pom.xml (父工程)
```

---

## 附录：核心问题与解决方案速查表

| 问题类别   | 具体问题                    | 解决方案                            |
| ---------- | --------------------------- | ----------------------------------- |
| 依赖版本   | Netty 版本冲突              | 引入 netty-bom 统一版本             |
| 依赖版本   | MySQL 驱动名变更            | 改用 mysql-connector-j              |
| JWT        | 字段名不一致                | 统一为 userId/username              |
| 事务       | 自调用事务失效              | @Lazy 注入代理调用                  |
| 缓存       | PageInfo 反序列化失败       | TypeReference + convertToTargetType |
| 缓存       | KEYS 命令阻塞 Redis         | 改用 SCAN + 批量 DEL                |
| 分布式锁   | 锁超时后未释放              | finally + isHeldByCurrentThread     |
| WebSocket  | 离线消息未补发              | Redis List 暂存，上线推送           |
| OSS        | 越权上传风险                | Policy 硬编码 doctorId              |
| 审计       | 日志记录影响性能            | @Async 异步保存                     |
| 部署       | 多实例 WebSocket 会话不共享 | Redis 存储在线状态                  |
| 数据一致性 | 服务重启任务丢失            | @PostConstruct 扫描补偿             |

---

**项目状态：** ✅ 所有功能开发完成，文档齐全，可随时部署上线。  
**后续规划：** 真实微信支付接入（需企业资质）、WebSocket 集群支持（Redis Pub/Sub）、AI 智能导诊。
