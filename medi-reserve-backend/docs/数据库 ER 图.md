# MediReserve 智慧医疗预约挂号平台 — 数据库 ER 图 

> **版本**：v1.0-SNAPSHOT  
> **数据库**：MySQL 8.0+ (InnoDB, utf8mb4)  
> **最后更新**：2026-07-23

---

## 一、表清单（共 14 张）

| 序号 | 表名 | 注释 | 主要用途 |
|------|------|------|---------|
| 1 | `admin` | 管理员表 | 管理后台登录账号 |
| 2 | `patient` | 患者表 | 患者注册/登录账号 |
| 3 | `doctor` | 医生表 | 医生注册/登录账号（关联科室、职称） |
| 4 | `doctor_audit` | 医生审核资料表 | 医生注册审核、证件变更审核 |
| 5 | `department` | 科室字典表 | 科室下拉选项 |
| 6 | `title` | 职称字典表 | 职称下拉选项 |
| 7 | `schedule` | 排班表 | 医生排班 + 号源管理 |
| 8 | `appointment` | 预约记录表 | 患者预约挂号记录 |
| 9 | `evaluation` | 就诊评价表 | 患者对医生的评分评价 |
| 10 | `consultation_message` | 问诊消息表 | WebSocket 在线问诊聊天记录 |
| 11 | `permission` | 权限表 | RBAC 权限定义 |
| 12 | `role` | 角色表 | RBAC 角色定义 |
| 13 | `role_permission` | 角色权限关联表 | 多对多中间表 |
| 14 | `operation_log` | 操作日志表 | 管理员审计日志 |

---

## 二、表结构详细说明

### 2.1 管理员表 (`admin`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 管理员ID |
| `username` | varchar(50) | UNIQUE | — | 用户名（登录账号） |
| `password` | varchar(100) | — | — | 密码（BCrypt 加密） |
| `name` | varchar(50) | — | — | 真实姓名 |
| `phone` | varchar(20) | — | — | 手机号 |
| `email` | varchar(50) | — | — | 邮箱 |
| `role` | tinyint | — | — | 角色：1=超级管理员，2=普通管理员 |
| `status` | tinyint | — | — | 账号状态：0=禁用，1=正常 |
| `last_login_ip` | varchar(50) | — | — | 最后登录IP |
| `last_login_time` | datetime | — | — | 最后登录时间 |
| `created_at` | datetime | — | — | 创建时间 |
| `updated_at` | datetime | — | — | 更新时间（自动更新） |

### 2.2 患者表 (`patient`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 患者ID |
| `name` | varchar(50) | — | — | 姓名 |
| `phone` | varchar(20) | UNIQUE | — | 手机号（登录账号） |
| `password` | varchar(100) | — | — | 密码（BCrypt 加密） |
| `id_card` | varchar(18) | — | — | 身份证号 |
| `gender` | tinyint | — | — | 性别：0=未知，1=男，2=女 |
| `status` | tinyint | — | — | 账号状态：0=禁用，1=正常 |
| `created_at` | datetime | — | — | 注册时间 |
| `updated_at` | datetime | — | — | 更新时间（自动更新） |

### 2.3 医生表 (`doctor`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 医生ID |
| `name` | varchar(50) | — | — | 姓名 |
| `phone` | varchar(20) | UNIQUE | — | 手机号（登录账号） |
| `password` | varchar(100) | — | — | 密码（BCrypt 加密） |
| `id_card` | varchar(18) | — | — | 身份证号 |
| `gender` | tinyint | — | — | 性别：0=未知，1=男，2=女 |
| `birth_date` | date | — | — | 出生日期 |
| `department_id` | bigint | — | FK → `department.id` | 科室ID |
| `title_id` | bigint | — | FK → `title.id` | 职称ID |
| `status` | tinyint | — | — | 账号状态：0=禁用，1=正常 |
| `created_at` | datetime | — | — | 注册时间 |
| `updated_at` | datetime | — | — | 更新时间（自动更新） |

### 2.4 医生审核资料表 (`doctor_audit`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 主键ID |
| `doctor_id` | bigint | UNIQUE | FK → `doctor.id` (CASCADE) | 医生ID（一对一） |
| `certificate_url` | varchar(255) | — | — | 执业证书图片URL |
| `qualification_url` | varchar(255) | — | — | 资格证图片URL |
| `specialty` | varchar(255) | — | — | 擅长领域 |
| `introduction` | text | — | — | 个人简介 |
| `avatar` | varchar(255) | — | — | 头像URL |
| `audit_status` | tinyint | — | — | 注册审核状态：0=待审核，1=通过，2=驳回 |
| `audit_remark` | varchar(255) | — | — | 审核备注 |
| `audit_time` | datetime | — | — | 审核时间 |
| `auditor_id` | bigint | — | — | 审核人（管理员ID） |
| `pending_certificate_url` | varchar(255) | — | — | 待审核执业证书URL |
| `pending_qualification_url` | varchar(255) | — | — | 待审核资格证URL |
| `cert_audit_status` | tinyint | — | — | 证件审核状态：0=待审核，1=已通过，2=已驳回 |
| `cert_audit_remark` | varchar(255) | — | — | 证件审核备注 |
| `cert_audit_time` | datetime | — | — | 证件审核时间 |
| `cert_auditor_id` | bigint | — | — | 证件审核人ID |
| `created_at` | datetime | — | — | 创建时间 |
| `updated_at` | datetime | — | — | 更新时间 |

### 2.5 科室字典表 (`department`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 科室ID |
| `name` | varchar(50) | UNIQUE | — | 科室名称 |
| `sort_order` | int | — | — | 排序序号（越小越靠前） |
| `created_at` | datetime | — | — | 创建时间 |
| `updated_at` | datetime | — | — | 更新时间 |

### 2.6 职称字典表 (`title`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 职称ID |
| `name` | varchar(30) | UNIQUE | — | 职称名称 |
| `sort_order` | int | — | — | 排序序号 |
| `created_at` | datetime | — | — | 创建时间 |
| `updated_at` | datetime | — | — | 更新时间 |

### 2.7 排班表 (`schedule`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 排班ID |
| `doctor_id` | bigint | — | FK → `doctor.id` | 医生ID |
| `schedule_date` | date | — | — | 排班日期 (YYYY-MM-DD) |
| `period` | tinyint | — | — | 时段：1=上午，2=下午 |
| `max_count` | int | — | — | 最大挂号数 |
| `remaining_count` | int | — | — | 剩余号源（实时扣减） |
| `status` | tinyint | — | — | 状态：1=正常，2=已停诊，3=已满 |
| `created_at` | datetime | — | — | 创建时间 |
| `updated_at` | datetime | — | — | 更新时间 |

> **唯一约束**：`(doctor_id, schedule_date, period)` — 同一医生同一日期同一时段只能有一条排班

### 2.8 预约表 (`appointment`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 预约ID |
| `appointment_no` | varchar(32) | UNIQUE | — | 预约单号（业务唯一标识） |
| `schedule_id` | bigint | — | FK → `schedule.id` | 排班ID |
| `patient_id` | bigint | — | FK → `patient.id` | 患者ID |
| `doctor_id` | bigint | — | FK → `doctor.id` | 医生ID（冗余字段，方便查询） |
| `status` | tinyint | — | — | 状态：0=待支付，1=已支付，2=已就诊，3=已取消，4=已过期 |
| `created_at` | datetime | — | — | 创建时间 |
| `updated_at` | datetime | — | — | 更新时间 |

### 2.9 评价表 (`evaluation`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 评价ID |
| `appointment_id` | bigint | UNIQUE | FK → `appointment.id` | 预约ID（一个预约只能评价一次） |
| `patient_id` | bigint | — | FK → `patient.id` | 患者ID（评价人） |
| `doctor_id` | bigint | — | FK → `doctor.id` | 医生ID（被评价人） |
| `schedule_id` | bigint | — | FK → `schedule.id` | 排班ID（冗余，方便查询） |
| `score` | tinyint | — | — | 评分：1-5 星 |
| `content` | varchar(500) | — | — | 评价内容（最多 500 字） |
| `is_anonymous` | tinyint | — | — | 是否匿名：0=不匿名，1=匿名 |
| `status` | tinyint | — | — | 状态：1=已发布，2=已隐藏 |
| `created_at` | datetime | — | — | 创建时间 |
| `updated_at` | datetime | — | — | 更新时间 |

### 2.10 问诊消息表 (`consultation_message`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 消息ID |
| `appointment_id` | bigint | — | FK → `appointment.id` | 预约ID |
| `sender_id` | bigint | — | — | 发送者ID |
| `receiver_id` | bigint | — | — | 接收者ID |
| `sender_role` | varchar(20) | — | — | 发送者角色：PATIENT / DOCTOR |
| `content` | varchar(1000) | — | — | 消息内容（已防 XSS 过滤） |
| `msg_type` | tinyint | — | — | 消息类型：1=文本，2=图片（预留） |
| `is_read` | tinyint | — | — | 是否已读：0=未读，1=已读 |
| `send_time` | datetime | — | — | 发送时间 |

### 2.11 权限表 (`permission`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 权限ID |
| `parent_id` | bigint | — | — | 父权限ID（0=顶级） |
| `code` | varchar(100) | UNIQUE | — | 权限代码（格式：`模块:子模块:操作`） |
| `name` | varchar(50) | — | — | 权限名称 |
| `type` | tinyint | — | — | 权限类型：1=菜单，2=按钮，3=接口 |
| `sort_order` | int | — | — | 排序序号 |
| `created_at` | datetime | — | — | 创建时间 |
| `updated_at` | datetime | — | — | 更新时间 |

### 2.12 角色表 (`role`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | int | PK | — | 角色ID（1=超级管理员，2=普通管理员） |
| `name` | varchar(50) | — | — | 角色名称 |
| `description` | varchar(200) | — | — | 角色描述 |
| `created_at` | datetime | — | — | 创建时间 |
| `updated_at` | datetime | — | — | 更新时间 |

### 2.13 角色权限关联表 (`role_permission`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `role_id` | int | PK (复合) | FK → `role.id` | 角色ID |
| `permission_id` | bigint | PK (复合) | FK → `permission.id` | 权限ID |

### 2.14 操作日志表 (`operation_log`)

| 字段 | 类型 | 主键 | 外键 | 注释 |
|------|------|------|------|------|
| `id` | bigint | PK, AUTO_INCREMENT | — | 日志ID |
| `admin_id` | bigint | — | — | 操作管理员ID |
| `admin_name` | varchar(50) | — | — | 操作管理员姓名 |
| `module` | varchar(50) | — | — | 操作模块 |
| `operation` | varchar(100) | — | — | 操作描述 |
| `method` | varchar(10) | — | — | 请求方法（GET/POST/PUT/DELETE） |
| `path` | varchar(200) | — | — | 请求路径 |
| `params` | text | — | — | 请求参数（JSON） |
| `ip` | varchar(50) | — | — | 客户端IP |
| `result` | tinyint | — | — | 操作结果：1=成功，0=失败 |
| `status_code` | int | — | — | HTTP 状态码 |
| `error_msg` | varchar(500) | — | — | 错误信息 |
| `duration_ms` | int | — | — | 操作耗时（毫秒） |
| `created_at` | datetime | — | — | 操作时间 |

---

## 三、Mermaid ER 图

```mermaid
erDiagram
    %% ====== 核心用户 ======
    admin {
        bigint id PK "管理员ID"
        varchar username UK "用户名"
        varchar password "密码(BCrypt)"
        varchar name "真实姓名"
        varchar phone "手机号"
        tinyint role "1=超级管理员 2=普通管理员"
        tinyint status "0=禁用 1=正常"
    }

    patient {
        bigint id PK "患者ID"
        varchar name "姓名"
        varchar phone UK "手机号(登录账号)"
        varchar password "密码(BCrypt)"
        varchar id_card "身份证号"
        tinyint gender "0=未知 1=男 2=女"
        tinyint status "0=禁用 1=正常"
    }

    doctor {
        bigint id PK "医生ID"
        varchar name "姓名"
        varchar phone UK "手机号(登录账号)"
        varchar password "密码(BCrypt)"
        bigint department_id FK "科室ID"
        bigint title_id FK "职称ID"
        tinyint status "0=禁用 1=正常"
    }

    %% ====== 字典表 ======
    department {
        bigint id PK "科室ID"
        varchar name UK "科室名称"
        int sort_order "排序序号"
    }

    title {
        bigint id PK "职称ID"
        varchar name UK "职称名称"
        int sort_order "排序序号"
    }

    %% ====== 医生审核 ======
    doctor_audit {
        bigint id PK "主键ID"
        bigint doctor_id UK_FK "医生ID(一对一)"
        varchar certificate_url "执业证书URL"
        varchar qualification_url "资格证URL"
        varchar specialty "擅长领域"
        text introduction "个人简介"
        tinyint audit_status "0=待审核 1=通过 2=驳回"
        varchar audit_remark "审核备注"
        bigint auditor_id "审核人ID"
        tinyint cert_audit_status "证件审核状态"
    }

    %% ====== 排班与预约 ======
    schedule {
        bigint id PK "排班ID"
        bigint doctor_id FK "医生ID"
        date schedule_date "排班日期"
        tinyint period "1=上午 2=下午"
        int max_count "最大挂号数"
        int remaining_count "剩余号源"
        tinyint status "1=正常 2=停诊 3=已满"
    }

    appointment {
        bigint id PK "预约ID"
        varchar appointment_no UK "预约单号"
        bigint schedule_id FK "排班ID"
        bigint patient_id FK "患者ID"
        bigint doctor_id FK "医生ID(冗余)"
        tinyint status "0=待支付 1=已支付 2=已就诊 3=已取消 4=已过期"
    }

    %% ====== 评价与问诊 ======
    evaluation {
        bigint id PK "评价ID"
        bigint appointment_id UK_FK "预约ID(一对一)"
        bigint patient_id FK "患者ID"
        bigint doctor_id FK "医生ID"
        bigint schedule_id FK "排班ID(冗余)"
        tinyint score "评分 1-5"
        varchar content "评价内容(500字)"
        tinyint is_anonymous "0=不匿名 1=匿名"
        tinyint status "1=已发布 2=已隐藏"
    }

    consultation_message {
        bigint id PK "消息ID"
        bigint appointment_id FK "预约ID"
        bigint sender_id "发送者ID"
        bigint receiver_id "接收者ID"
        varchar sender_role "PATIENT/DOCTOR"
        varchar content "消息内容(1000字)"
        tinyint msg_type "1=文本 2=图片"
        tinyint is_read "0=未读 1=已读"
        datetime send_time "发送时间"
    }

    %% ====== RBAC 权限 ======
    permission {
        bigint id PK "权限ID"
        bigint parent_id "父权限ID"
        varchar code UK "权限代码"
        varchar name "权限名称"
        tinyint type "1=菜单 2=按钮 3=接口"
    }

    role {
        int id PK "角色ID"
        varchar name "角色名称"
        varchar description "角色描述"
    }

    role_permission {
        int role_id PK_FK "角色ID"
        bigint permission_id PK_FK "权限ID"
    }

    %% ====== 操作日志 ======
    operation_log {
        bigint id PK "日志ID"
        bigint admin_id "操作管理员ID"
        varchar admin_name "操作管理员姓名"
        varchar module "操作模块"
        varchar operation "操作描述"
        varchar method "请求方法"
        varchar path "请求路径"
        text params "请求参数(JSON)"
        varchar ip "客户端IP"
        tinyint result "1=成功 0=失败"
        int duration_ms "耗时(毫秒)"
    }

    %% ====== 关系定义 ======

    %% 医生 ↔ 科室/职称（多对一）
    doctor }o--|| department : "belongs to"
    doctor }o--|| title : "has title"

    %% 医生 ↔ 审核资料（一对一）
    doctor ||--|| doctor_audit : "has audit info"

    %% 管理员审核医生
    admin ||--o{ doctor_audit : "audits"

    %% 医生 ↔ 排班（一对多）
    doctor ||--o{ schedule : "creates schedules"

    %% 排班 ↔ 预约（一对多）
    schedule ||--o{ appointment : "contains"

    %% 患者 ↔ 预约（一对多）
    patient ||--o{ appointment : "makes"

    %% 医生 ↔ 预约（一对多，冗余关系）
    doctor ||--o{ appointment : "receives"

    %% 预约 ↔ 评价（一对一）
    appointment ||--|| evaluation : "may have"

    %% 评价 ↔ 患者/医生（多对一）
    patient ||--o{ evaluation : "writes"
    doctor ||--o{ evaluation : "receives"

    %% 问诊消息 ↔ 预约（多对一）
    appointment ||--o{ consultation_message : "contains chats"

    %% RBAC 关系
    role ||--o{ role_permission : "assigned"
    permission ||--o{ role_permission : "granted"
```

---

## 四、表间关系详细说明

### 4.1 核心业务域

```
doctor ──┬── 1:1 ── doctor_audit    （医生资料审核，级联删除）
         ├── N:1 ── department      （所属科室）
         ├── N:1 ── title           （职称）
         ├── 1:N ── schedule        （排班，一个医生多条排班）
         ├── 1:N ── appointment     （接收预约，冗余字段）
         └── 1:N ── evaluation      （被评价）

patient ──┬── 1:N ── appointment    （创建预约）
          └── 1:N ── evaluation     （发表评价）

schedule ── 1:N ── appointment      （排班下的预约记录）

appointment ──┬── 1:1 ── evaluation         （一个预约最多一条评价）
              └── 1:N ── consultation_message（问诊聊天记录）
```

### 4.2 RBAC 权限域

```
role ── 1:N ── role_permission ── N:1 ── permission
```

- 一个角色拥有多个权限
- 一个权限可分配给多个角色
- 通过 `role_permission` 中间表实现多对多

### 4.3 审计日志域

```
admin ── 1:N ── operation_log
```

- `admin_id` 字段记录操作管理员ID（非外键约束，保留历史）
- 日志独立存在，管理员删除后日志保留

### 4.4 关键约束说明

| 约束类型 | 表 | 说明 |
|---------|-----|------|
| UNIQUE `phone` | `patient` | 手机号全局唯一 |
| UNIQUE `phone` | `doctor` | 手机号全局唯一 |
| UNIQUE `username` | `admin` | 用户名全局唯一 |
| UNIQUE `doctor_id` | `doctor_audit` | 一个医生只有一条审核记录 |
| UNIQUE `(doctor_id, date, period)` | `schedule` | 同一医生同一时段仅一条排班 |
| UNIQUE `appointment_no` | `appointment` | 预约单号全局唯一 |
| UNIQUE `appointment_id` | `evaluation` | 一个预约只能评价一次 |
| FK CASCADE `doctor_id` | `doctor_audit` | 删除医生时级联删除审核资料 |
| FK UNIQUE `code` | `permission` | 权限代码全局唯一 |
| PK COMPOSITE `(role_id, permission_id)` | `role_permission` | 复合主键防重复 |

---

## 五、初始数据说明

| 表 | 数据内容 |
|-----|---------|
| `department` | 8 个科室：内科、外科、儿科、妇产科、骨科、眼科、皮肤科、中医科 |
| `title` | 5 个职称：主任医师、副主任医师、主治医师、住院医师、医师 |
| `admin` | `admin` (超级管理员) + `admin_normal` (普通管理员)，密码均为 `123456` (BCrypt) |
| `role` | 超级管理员 (id=1) + 普通管理员 (id=2) |
| `permission` | 13 条权限（4 个菜单 + 9 个接口），树形结构 |
| `role_permission` | 超级管理员拥有全部 13 条权限，普通管理员拥有 4 条查看权限 |

---

> **脚本位置**：`deploy/mysql/init.sql`  
> **执行方式**：Docker Compose 自动执行，或手动 `SOURCE deploy/mysql/init.sql`