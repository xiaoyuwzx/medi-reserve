# MediReserve 智慧医疗预约挂号平台 — 部署文档 

> **面向对象**：运维人员 / 后端开发人员  
> **版本**：v1.0-SNAPSHOT  
> **最后更新**：2026-07-23

---

## 目录

1. [环境要求](#1-环境要求)
2. [项目结构说明](#2-项目结构说明)
3. [配置文件说明](#3-配置文件说明)
4. [数据库初始化步骤](#4-数据库初始化步骤)
5. [Docker Compose 部署](#5-docker-compose-部署)
6. [手动 JAR 包部署](#6-手动-jar-包部署)
7. [服务端口一览表](#7-服务端口一览表)
8. [日志查看方式](#8-日志查看方式)
9. [常见问题排查](#9-常见问题排查)

---

## 1. 环境要求

| 组件 | 推荐版本 | 说明 |
|------|---------|------|
| JDK | **17** | Spring Boot 3.3.6 最低要求 JDK 17 |
| MySQL | **8.0+** | 需要 utf8mb4 编码（存储 emoji） |
| Redis | **7.0+** | 用于分布式锁、缓存、WebSocket 会话管理 |
| Maven | **3.9.x** | 构建工具 |
| Docker | **24.x+** | 容器化部署（可选） |
| Docker Compose | **2.x+** | 容器编排（可选） |

> **注意**：如果使用 Docker Compose 部署，所有中间件（MySQL、Redis）和微服务均在容器内运行，宿主机只需安装 Docker 和 Docker Compose。

---

## 2. 项目结构说明

```
medi-reserve-backend/
├── pom.xml                          # 父工程 POM（版本锁定、插件管理）
├── docker-compose.yml               # Docker Compose 编排文件
├── Dockerfile                       # 多阶段构建镜像
├── .env.example                     # 环境变量模板（复制为 .env）
├── deploy.sh                        # 部署辅助脚本
├── deploy/
│   └── mysql/
│       ├── init.sql                 # 数据库建表 + 初始数据
│       └── conf.d/                  # MySQL 自定义配置（可选）
├── medi-common/                     # 公共模块（实体类、DTO、工具类、配置）
│   ├── pom.xml
│   └── src/main/java/com/medireserve/common/
├── medi-patient/                    # 患者端服务（端口 8081）
│   ├── pom.xml
│   └── src/main/resources/
│       ├── application.yml          # 默认配置（本地开发）
│       └── application-docker.yml   # Docker 环境覆盖配置
├── medi-doctor/                     # 医生端服务（端口 8082）
│   ├── pom.xml
│   └── src/main/resources/
│       ├── application.yml
│       └── application-docker.yml
├── medi-admin/                      # 管理端服务（端口 8083）
│   ├── pom.xml
│   └── src/main/resources/
│       ├── application.yml
│       └── application-docker.yml
└── medi-websocket/                  # WebSocket 服务（端口 8084）
    ├── pom.xml
    └── src/main/resources/
        ├── application.yml
        └── application-docker.yml
```

---

## 3. 配置文件说明

### 3.1 父工程 POM (`pom.xml`)

关键版本号在 `<properties>` 中统一定义：

| 属性 | 版本 |
|------|------|
| `java.version` | 17 |
| Spring Boot | 3.3.6 |
| MyBatis Spring Boot | 3.0.3 |
| Knife4j (Swagger) | 4.5.0 |
| Redisson | 3.27.2 |
| Hutool | 5.8.26 |
| JJWT | 0.12.5 |
| Lombok | 1.18.34 |
| PageHelper | 2.1.0 |
| Caffeine | 3.1.8 |
| Guava | 33.2.0-jre |
| Netty | 4.1.115.Final |

### 3.2 各模块 `application.yml` 关键配置项

所有模块共享相同的数据库和 Redis 基础配置：

```yaml
# ========== 服务器端口 ==========
server:
  port: 8081 (或 8082/8083/8084)

# ========== 数据源 ==========
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/medi_reserve?useSSL=false&allowPublicKeyRetrieval=true&...
    username: root
    password: 123456

# ========== Redis ==========
spring:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD:}     # 支持环境变量，默认为空
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

# ========== JWT ==========
jwt:
  secret: "your-256-bit-secret-key-must-be-long-enough-1234567890"
  expiration: 7200000                 # Token 有效期：2 小时（毫秒）
  interceptor:
    enabled: true                     # 是否启用 JWT 拦截器
    exclude-paths:                    # 白名单路径（无需 Token）
      - /patient/login
      - /patient/register
      - /patient/dict/**
      - ...

# ========== MyBatis ==========
mybatis:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.medireserve.common.entity
  configuration:
    map-underscore-to-camel-case: true

# ========== Actuator 健康检查 ==========
management:
  endpoints:
    web:
      exposure:
        include: health
```

### 3.3 医生端特殊配置：阿里云 OSS

仅在 `medi-doctor` 的 `application.yml` 中配置：

```yaml
aliyun:
  oss:
    endpoint: oss-cn-wuhan-lr.aliyuncs.com
    bucket: medi-reserve-files
    sts-endpoint: sts.aliyuncs.com
    access-key-id: ${ALIBABA_CLOUD_ACCESS_KEY}
    access-key-secret: ${ALIBABA_CLOUD_SECRET_KEY}
    role-arn: acs:ram::1353664742775914:role/medi-oss-role
    timeout: 1800                    # STS 临时凭证有效期（秒）
    base-dir: medi                   # 上传根目录
```

### 3.4 Docker 环境配置文件 (`application-docker.yml`)

每个模块都有一个 `application-docker.yml`，在 Docker 环境中通过 `SPRING_PROFILES_ACTIVE=docker` 激活。Docker 环境下的差异：

- 数据库连接：`mysql` → Docker DNS 服务名
- Redis 连接：`redis` → Docker DNS 服务名
- 密码从环境变量注入
- 日志级别提升为 INFO

### 3.5 环境变量（`.env` 文件）

从 `.env.example` 复制为 `.env` 并填写实际值：

```bash
MYSQL_ROOT_PASSWORD=123456
MYSQL_DATABASE=medi_reserve
REDIS_PASSWORD=
JWT_SECRET=your-256-bit-secret-key-must-be-long-enough-1234567890
ALIYUN_ACCESS_KEY_ID=
ALIYUN_ACCESS_KEY_SECRET=
ALIYUN_ROLE_ARN=acs:ram::123456789:role/medi-oss-role
```

---

## 4. 数据库初始化步骤

### 方式一：Docker 自动初始化（推荐）

Docker Compose 启动时会自动挂载 `deploy/mysql/init.sql` 到 MySQL 容器的 `/docker-entrypoint-initdb.d/` 目录，MySQL 首次启动时自动执行该脚本。

**无需手动操作**。

### 方式二：手动导入 SQL

适用于已有 MySQL 实例的场景：

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS medi_reserve CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入初始化脚本
USE medi_reserve;
SOURCE deploy/mysql/init.sql;
```

### 初始化内容包括

- **14 张数据表**（详见 ER 图文档）
- **科室字典**：内科、外科、儿科、妇产科、骨科、眼科、皮肤科、中医科
- **职称字典**：主任医师、副主任医师、主治医师、住院医师、医师
- **管理员账号**：
  - 超级管理员：`admin` / `123456`
  - 普通管理员：`admin_normal` / `123456`
- **RBAC 权限数据**：13 条权限 + 角色 + 角色-权限关联

---

## 5. Docker Compose 部署

### 5.1 前置准备

确保已安装 Docker 和 Docker Compose：

```bash
docker --version
docker compose version
```

### 5.2 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env，填写实际配置（生产环境务必修改密码和 JWT 密钥）
vi .env
```

### 5.3 构建并启动所有服务

```bash
# 构建镜像并启动所有服务（首次启动会下载镜像 + 构建项目）
docker compose up -d --build

# 仅启动服务（不重新构建）
docker compose up -d
```

启动顺序：
1. MySQL 容器 → 健康检查通过
2. Redis 容器 → 健康检查通过
3. 四个微服务（并行启动）→ 依赖 MySQL 和 Redis 健康
4. Nginx 容器（当前已注释）

### 5.4 停止服务

```bash
# 停止所有服务（保留数据卷）
docker compose down

# 停止所有服务并删除数据卷（清空数据库和 Redis 数据）
docker compose down -v
```

### 5.5 查看日志

```bash
# 查看所有服务日志（实时滚动）
docker compose logs -f

# 查看指定服务日志
docker compose logs -f patient
docker compose logs -f doctor
docker compose logs -f admin
docker compose logs -f websocket
docker compose logs -f mysql
docker compose logs -f redis

# 查看最近 100 行日志
docker compose logs --tail=100 patient
```

### 5.6 重启单个服务

```bash
docker compose restart patient
```

### 5.7 Docker Compose 服务配置概览

| 服务 | 容器名 | 端口映射 | 内存限制 | 健康检查 |
|------|--------|---------|---------|---------|
| MySQL | medi-mysql | 3306:3306 | — | `mysqladmin ping` |
| Redis | medi-redis | 6379:6379 | — | `redis-cli ping` |
| Patient | medi-patient | 8081:8081 | 128-256MB | `/actuator/health` |
| Doctor | medi-doctor | 8082:8082 | 128-256MB | `/actuator/health` |
| Admin | medi-admin | 8083:8083 | 128-256MB | `/actuator/health` |
| WebSocket | medi-websocket | 8084:8084 | 128-256MB | `/actuator/health` |
| Nginx | medi-nginx | 80:80, 443:443 | — | (已注释) |

---

## 6. 手动 JAR 包部署

### 6.1 打包

在项目根目录执行（已配置跳过测试，9 分钟构建完成）：

```bash
mvn clean package -DskipTests
```

> **说明**：父 POM 中 `maven-surefire-plugin` 已配置 `<skipTests>true</skipTests>`，构建时默认跳过测试。

### 6.2 启动命令

| 启动顺序 | 模块 | JAR 包路径 | 启动命令 | 端口 |
|---------|------|-----------|---------|------|
| — | MySQL + Redis | — | 需提前启动 | 3306 / 6379 |
| 1 | 患者端 | `medi-patient/target/medi-patient-*.jar` | `java -jar medi-patient/target/medi-patient-*.jar` | 8081 |
| 2 | 医生端 | `medi-doctor/target/medi-doctor-*.jar` | `java -jar medi-doctor/target/medi-doctor-*.jar` | 8082 |
| 3 | 管理端 | `medi-admin/target/medi-admin-*.jar` | `java -jar medi-admin/target/medi-admin-*.jar` | 8083 |
| 4 | WebSocket | `medi-websocket/target/medi-websocket-*.jar` | `java -jar medi-websocket/target/medi-websocket-*.jar` | 8084 |

> **注意**：`medi-common` 是公共模块，不独立运行，不需要单独启动。

### 6.3 指定 JVM 参数启动

```bash
java -Xms128m -Xmx256m -jar medi-patient/target/medi-patient-*.jar
```

### 6.4 使用 Docker Profile

如果本地有 Docker 运行 MySQL 和 Redis，可以使用 Docker Profile：

```bash
java -jar -Dspring.profiles.active=docker \
  -DSPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/medi_reserve?..." \
  medic-patient/target/medi-patient-*.jar
```

### 6.5 验证启动成功

```bash
# 使用 curl 检查健康端点
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
```

预期返回：
```json
{"status":"UP"}
```

---

## 7. 服务端口一览表

| 服务 | 端口 | 说明 |
|------|------|------|
| **MySQL** | 3306 | 数据库服务 |
| **Redis** | 6379 | 缓存服务 |
| **患者端 (Patient)** | **8081** | 患者注册/登录、科室医生浏览、预约挂号、支付、评价 |
| **医生端 (Doctor)** | **8082** | 医生注册/登录、排班管理、在线问诊、数据统计 |
| **管理端 (Admin)** | **8083** | 管理员登录、医生审核、统计看板、操作日志、权限管理 |
| **WebSocket** | **8084** | STOMP over WebSocket，在线问诊实时消息 |
| Nginx | 80 / 443 | 反向代理（当前 Docker Compose 中已注释，待启用） |

### Swagger/Knife4j 文档地址

| 服务 | API 文档 |
|------|---------|
| 患者端 | `http://localhost:8081/doc.html` |
| 医生端 | `http://localhost:8082/doc.html` |
| 管理端 | `http://localhost:8083/doc.html` |
| WebSocket | `http://localhost:8084/doc.html` |

---

## 8. 日志查看方式

### 8.1 Docker 环境

```bash
# 实时查看所有服务日志
docker compose logs -f

# 查看指定服务
docker compose logs -f patient

# 查看最近日志
docker compose logs --tail=200 patient
```

### 8.2 手动部署（JAR 包）

MyBatis SQL 日志已开启（`StdOutImpl`），直接输出到控制台。

```bash
# 控制台日志
java -jar medi-patient/target/medi-patient-*.jar

# 重定向到文件
java -jar medi-patient/target/medi-patient-*.jar > patient.log 2>&1 &
tail -f patient.log
```

### 8.3 日志级别

默认配置：

| 环境 | 业务日志 (com.medireserve) | 框架日志 |
|------|--------------------------|---------|
| 本地开发 (application.yml) | MyBatis DEBUG (StdOut) | Spring INFO |
| Docker (application-docker.yml) | INFO | WARN |

如需调整日志级别，在启动参数中添加：

```bash
java -jar medi-patient/target/medi-patient-*.jar --logging.level.com.medireserve=DEBUG
```

---

## 9. 常见问题排查

### 9.1 端口冲突

**现象**：启动时报错 `Port xxxx was already in use`

**解决**：
```bash
# Windows 查看端口占用
netstat -ano | findstr :8081

# Linux 查看端口占用
lsof -i :8081

# 修改配置文件中的端口，或终止占用进程
```

### 9.2 MySQL 连接失败

**现象**：`CommunicationsException: Communications link failure`

**排查步骤**：
1. 确认 MySQL 服务是否运行：`docker compose ps mysql`
2. 确认连接信息是否正确：主机、端口、用户名、密码
3. 本地开发环境确认 MySQL 允许远程连接
4. Docker 环境确认服务名 `mysql` 在同一网络内

### 9.3 Redis 连接失败

**现象**：`Unable to connect to Redis`

**排查步骤**：
1. 确认 Redis 服务是否运行：`docker compose ps redis`
2. 检查 Redis 密码配置（`REDIS_PASSWORD` 环境变量）
3. 本地开发默认无密码，Docker 环境可通过 `REDIS_PASSWORD` 设置

### 9.4 数据库表不存在

**现象**：启动后访问 API 报错 `Table 'medi_reserve.xxx' doesn't exist`

**解决**：
1. 确认 `deploy/mysql/init.sql` 已执行
2. Docker 环境：删除已有的 MySQL 数据卷后重新启动
   ```bash
   docker compose down -v
   docker compose up -d
   ```

### 9.5 JWT Token 无效

**现象**：返回 401 或 Token 解析失败

**排查**：
1. 确认各模块 `jwt.secret` 配置一致（四个模块需使用相同的密钥）
2. Token 有效期默认为 2 小时（`jwt.expiration: 7200000`）

### 9.6 Docker 构建慢

**原因**：多阶段构建需要下载 Maven 依赖

**优化**：
- 已配置 Docker 镜像加速器（使用 eclipse-temurin 官方镜像）
- 首次构建会下载所有依赖，后续构建会使用缓存

### 9.7 跨模块调用失败

**注意**：当前项目各模块之间**不通过 HTTP 相互调用**，而是通过共享 `medi-common` 模块来共享实体类、Mapper、工具类等。每个模块都是独立的 Spring Boot 应用，拥有自己的数据源连接。

如果需要模块间通信（如患者端调用 WebSocket 服务），目前需要通过前端或 Nginx 网关路由，属于**待开发**功能。

---

> **技术支持**：如有其他问题，请查阅项目 [README](../README.md) 或联系开发团队。