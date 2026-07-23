-- ============================================================
-- MediReserve 智慧医疗平台 - 数据库初始化脚本
-- 版本：v1.0
-- 说明：包含所有表结构、初始数据（管理员、角色、权限等）
-- 适用环境：MySQL 8.0+
-- 执行方式：在容器启动时自动执行，或手动导入
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 管理员表（admin）
-- 存储系统管理员账号，用于管理后台登录
-- ============================================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
                         `username` varchar(50) NOT NULL COMMENT '用户名（登录账号）',
                         `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
                         `name` varchar(50) NOT NULL COMMENT '真实姓名',
                         `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                         `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
                         `role` tinyint DEFAULT 2 COMMENT '角色：1超级管理员 2普通管理员',
                         `status` tinyint DEFAULT 1 COMMENT '账号状态：0禁用 1正常',
                         `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
                         `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
                         `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `username` (`username`),
                         KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表';

-- ============================================================
-- 2. 患者表（patient）
-- 存储患者注册信息，用于患者端登录和身份识别
-- ============================================================
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者ID',
                           `name` varchar(50) NOT NULL COMMENT '姓名',
                           `phone` varchar(20) NOT NULL COMMENT '手机号（登录账号）',
                           `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
                           `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
                           `gender` tinyint DEFAULT 0 COMMENT '性别：0未知 1男 2女',
                           `status` tinyint DEFAULT 1 COMMENT '账号状态：0禁用 1正常',
                           `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                           `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `phone` (`phone`),
                           KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者认证表';

-- ============================================================
-- 3. 医生表（doctor）
-- 存储医生基本信息，与 doctor_audit 表联动
-- ============================================================
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生ID',
                          `name` varchar(50) NOT NULL COMMENT '姓名',
                          `phone` varchar(20) NOT NULL COMMENT '手机号（登录账号）',
                          `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
                          `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
                          `gender` tinyint DEFAULT 0 COMMENT '性别：0未知 1男 2女',
                          `birth_date` date DEFAULT NULL COMMENT '出生日期',
                          `department_id` bigint DEFAULT NULL COMMENT '科室ID',
                          `title_id` bigint DEFAULT NULL COMMENT '职称ID',
                          `status` tinyint DEFAULT 1 COMMENT '账号状态：0禁用 1正常',
                          `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                          `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `phone` (`phone`),
                          KEY `idx_phone` (`phone`),
                          KEY `fk_doctor_department` (`department_id`),
                          KEY `fk_doctor_title` (`title_id`),
                          CONSTRAINT `fk_doctor_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                          CONSTRAINT `fk_doctor_title` FOREIGN KEY (`title_id`) REFERENCES `title` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医生认证表';

-- ============================================================
-- 4. 医生审核资料表（doctor_audit）
-- 存储医生注册时的审核资料、专业信息及证件审核状态
-- ============================================================
DROP TABLE IF EXISTS `doctor_audit`;
CREATE TABLE `doctor_audit` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `doctor_id` bigint NOT NULL COMMENT '医生ID（关联doctor表）',
                                `certificate_url` varchar(255) DEFAULT NULL COMMENT '执业证书图片URL',
                                `qualification_url` varchar(255) DEFAULT NULL COMMENT '资格证图片URL',
                                `specialty` varchar(255) DEFAULT NULL COMMENT '擅长领域',
                                `introduction` text COMMENT '个人简介',
                                `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
                                `audit_status` tinyint DEFAULT 0 COMMENT '审核状态：0待审核 1审核通过 2审核驳回',
                                `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核备注',
                                `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
                                `auditor_id` bigint DEFAULT NULL COMMENT '审核人（管理员ID）',
                                `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                                `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 证件变更审核字段
                                `pending_certificate_url` varchar(255) DEFAULT NULL COMMENT '待审核执业证书URL',
                                `pending_qualification_url` varchar(255) DEFAULT NULL COMMENT '待审核资格证URL',
                                `cert_audit_status` tinyint DEFAULT NULL COMMENT '证件审核状态：0-待审核 1-已通过 2-已驳回',
                                `cert_audit_remark` varchar(255) DEFAULT NULL COMMENT '证件审核备注',
                                `cert_audit_time` datetime DEFAULT NULL COMMENT '证件审核时间',
                                `cert_auditor_id` bigint DEFAULT NULL COMMENT '证件审核人ID（管理员）',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `doctor_id` (`doctor_id`),
                                KEY `idx_doctor_id` (`doctor_id`),
                                KEY `idx_audit_status` (`audit_status`),
                                CONSTRAINT `doctor_audit_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医生审核资料表';

-- ============================================================
-- 5. 科室表（department）
-- 字典表，存储医院科室信息
-- ============================================================
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室ID',
                              `name` varchar(50) NOT NULL COMMENT '科室名称',
                              `sort_order` int DEFAULT 0 COMMENT '排序序号（数值越小越靠前）',
                              `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='科室字典表';

-- ============================================================
-- 6. 职称表（title）
-- 字典表，存储医生职称信息
-- ============================================================
DROP TABLE IF EXISTS `title`;
CREATE TABLE `title` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '职称ID',
                         `name` varchar(30) NOT NULL COMMENT '职称名称',
                         `sort_order` int DEFAULT 0 COMMENT '排序序号（数值越小越靠前）',
                         `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='职称字典表';

-- ============================================================
-- 7. 排班表（schedule）
-- 存储医生的排班信息，包含号源管理
-- ============================================================
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排班ID',
                            `doctor_id` bigint NOT NULL COMMENT '医生ID（关联doctor表）',
                            `schedule_date` date NOT NULL COMMENT '排班日期（格式：YYYY-MM-DD）',
                            `period` tinyint NOT NULL COMMENT '时段：1上午 2下午',
                            `max_count` int DEFAULT 20 COMMENT '最大挂号数（医生设置）',
                            `remaining_count` int DEFAULT 20 COMMENT '剩余号源（实时扣减）',
                            `status` tinyint DEFAULT 1 COMMENT '状态：1正常 2已停诊 3已满（系统自动设置）',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_doctor_date_period` (`doctor_id`,`schedule_date`,`period`),
                            KEY `idx_doctor_id` (`doctor_id`),
                            KEY `idx_date` (`schedule_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医生排班表';

-- ============================================================
-- 8. 预约表（appointment）
-- 存储患者挂号记录，状态流转管理
-- ============================================================
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
                               `appointment_no` varchar(32) NOT NULL COMMENT '预约单号（业务唯一标识）',
                               `schedule_id` bigint NOT NULL COMMENT '排班ID（关联schedule表）',
                               `patient_id` bigint NOT NULL COMMENT '患者ID（关联patient表）',
                               `doctor_id` bigint NOT NULL COMMENT '医生ID（冗余字段，方便查询）',
                               `status` tinyint DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已就诊 3已取消 4已过期',
                               `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（下单时间）',
                               `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `appointment_no` (`appointment_no`),
                               KEY `idx_schedule_id` (`schedule_id`),
                               KEY `idx_patient_id` (`patient_id`),
                               KEY `idx_doctor_id` (`doctor_id`),
                               KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约记录表';

-- ============================================================
-- 9. 评价表（evaluation）
-- 存储患者对医生的就诊评价，支持时间衰减算法
-- ============================================================
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
                              `appointment_id` bigint NOT NULL COMMENT '预约ID（关联appointment表）',
                              `patient_id` bigint NOT NULL COMMENT '患者ID（评价人）',
                              `doctor_id` bigint NOT NULL COMMENT '医生ID（被评价人）',
                              `schedule_id` bigint NOT NULL COMMENT '排班ID（冗余，方便查询）',
                              `score` tinyint NOT NULL COMMENT '评分：1-5星',
                              `content` varchar(500) DEFAULT NULL COMMENT '评价内容（选填，最多500字）',
                              `is_anonymous` tinyint DEFAULT 0 COMMENT '是否匿名：0-不匿名 1-匿名',
                              `status` tinyint DEFAULT 1 COMMENT '状态：1-已发布 2-已隐藏（用户删除/管理员下架）',
                              `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_appointment` (`appointment_id`) COMMENT '一个预约只能评价一次',
                              KEY `idx_doctor_id` (`doctor_id`) COMMENT '按医生查询评价',
                              KEY `idx_patient_id` (`patient_id`) COMMENT '按患者查询评价',
                              KEY `idx_doctor_created` (`doctor_id`,`created_at`) COMMENT '排行榜查询优化'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='就诊评价表';

-- ============================================================
-- 10. 问诊消息表（consultation_message）
-- 存储 WebSocket 在线问诊聊天记录
-- ============================================================
DROP TABLE IF EXISTS `consultation_message`;
CREATE TABLE `consultation_message` (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                        `appointment_id` bigint NOT NULL COMMENT '预约ID（关联appointment表）',
                                        `sender_id` bigint NOT NULL COMMENT '发送者ID',
                                        `receiver_id` bigint NOT NULL COMMENT '接收者ID',
                                        `sender_role` varchar(20) NOT NULL COMMENT '发送者角色：PATIENT/DOCTOR',
                                        `content` varchar(1000) NOT NULL COMMENT '消息内容（防XSS过滤）',
                                        `msg_type` tinyint DEFAULT 1 COMMENT '消息类型：1-文本，2-图片（预留）',
                                        `is_read` tinyint DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
                                        `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
                                        PRIMARY KEY (`id`),
                                        KEY `idx_appointment_id` (`appointment_id`),
                                        KEY `idx_send_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问诊聊天消息记录表';

-- ============================================================
-- 11. 权限表（permission）
-- RBAC 权限管理，定义系统中所有可控权限点
-- ============================================================
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID（主键）',
                              `parent_id` bigint DEFAULT 0 COMMENT '父权限ID（0表示顶级权限）',
                              `code` varchar(100) NOT NULL COMMENT '权限代码（全局唯一，格式：模块:子模块:操作）',
                              `name` varchar(50) NOT NULL COMMENT '权限名称（显示用）',
                              `type` tinyint NOT NULL COMMENT '权限类型：1-菜单，2-按钮，3-接口',
                              `sort_order` int DEFAULT 0 COMMENT '排序序号',
                              `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ============================================================
-- 12. 角色表（role）
-- RBAC 角色定义，与权限关联
-- ============================================================
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
                        `id` int NOT NULL COMMENT '角色ID（1-超级管理员，2-普通管理员）',
                        `name` varchar(50) NOT NULL COMMENT '角色名称',
                        `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
                        `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================
-- 13. 角色权限关联表（role_permission）
-- RBAC 多对多中间表
-- ============================================================
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
                                   `role_id` int NOT NULL COMMENT '角色ID（关联 role.id）',
                                   `permission_id` bigint NOT NULL COMMENT '权限ID（关联 permission.id）',
                                   PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================================
-- 14. 操作日志表（operation_log）
-- 记录管理员关键操作，用于审计
-- ============================================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                 `admin_id` bigint NOT NULL COMMENT '操作管理员ID',
                                 `admin_name` varchar(50) NOT NULL COMMENT '操作管理员姓名',
                                 `module` varchar(50) NOT NULL COMMENT '操作模块（如：审核管理、管理员管理等）',
                                 `operation` varchar(100) NOT NULL COMMENT '操作描述（如：审核通过医生、禁用管理员）',
                                 `method` varchar(10) NOT NULL COMMENT '请求方法（GET/POST/PUT/DELETE）',
                                 `path` varchar(200) NOT NULL COMMENT '请求路径',
                                 `params` text COMMENT '请求参数（JSON格式）',
                                 `ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
                                 `result` tinyint NOT NULL DEFAULT 1 COMMENT '操作结果：1-成功，0-失败',
                                 `status_code` int DEFAULT NULL COMMENT 'HTTP状态码',
                                 `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息（失败时记录）',
                                 `duration_ms` int DEFAULT NULL COMMENT '操作耗时（毫秒）',
                                 `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_admin_id` (`admin_id`),
                                 KEY `idx_created_at` (`created_at`),
                                 KEY `idx_module` (`module`),
                                 KEY `idx_result` (`result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- ============================================================
-- ============================================================
-- 初始数据插入
-- ============================================================

-- 1. 插入科室字典
INSERT IGNORE INTO `department` (`name`, `sort_order`) VALUES
                                                           ('内科', 1),
                                                           ('外科', 2),
                                                           ('儿科', 3),
                                                           ('妇产科', 4),
                                                           ('骨科', 5),
                                                           ('眼科', 6),
                                                           ('皮肤科', 7),
                                                           ('中医科', 8);

-- 2. 插入职称字典
INSERT IGNORE INTO `title` (`name`, `sort_order`) VALUES
                                                      ('主任医师', 4),
                                                      ('副主任医师', 3),
                                                      ('主治医师', 2),
                                                      ('住院医师', 1),
                                                      ('医师', 0);

-- 3. 插入默认管理员账号（密码均为：123456，BCrypt 加密）
-- 超级管理员 username: admin, 普通管理员 username: admin_normal
INSERT IGNORE INTO `admin` (`username`, `password`, `name`, `phone`, `role`, `status`) VALUES
                                                                                           ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', '13800000001', 1, 1),
                                                                                           ('admin_normal', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '普通管理员', '13800000002', 2, 1);

-- 4. 插入角色数据
INSERT IGNORE INTO `role` (`id`, `name`, `description`) VALUES
                                                            (1, '超级管理员', '拥有所有管理权限'),
                                                            (2, '普通管理员', '拥有部分管理权限（查看、审核等）');

-- 5. 插入权限数据（树形结构）
INSERT IGNORE INTO `permission` (`parent_id`, `code`, `name`, `type`, `sort_order`) VALUES
                                                                                        (0, 'admin:dashboard', '数据看板', 1, 1),
                                                                                        (0, 'admin:audit', '审核管理', 1, 2),
                                                                                        (0, 'admin:admin', '管理员管理', 1, 3),
                                                                                        (0, 'admin:log', '操作日志', 1, 4),
                                                                                        (1, 'admin:dashboard:view', '查看看板', 3, 1),
                                                                                        (2, 'admin:audit:view', '查看审核列表', 3, 1),
                                                                                        (2, 'admin:audit:approve', '审核通过', 3, 2),
                                                                                        (2, 'admin:audit:reject', '审核驳回', 3, 3),
                                                                                        (3, 'admin:admin:view', '查看管理员列表', 3, 1),
                                                                                        (3, 'admin:admin:create', '添加管理员', 3, 2),
                                                                                        (3, 'admin:admin:update', '修改管理员状态', 3, 3),
                                                                                        (4, 'admin:log:view', '查看日志', 3, 1),
                                                                                        (4, 'admin:log:delete', '删除日志', 3, 2);

-- 6. 为超级管理员分配所有权限（1~13）
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `permission`;

-- 7. 为普通管理员分配部分权限（查看类权限）
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`) VALUES
                                                                      (2, 1),  -- admin:dashboard:view
                                                                      (2, 2),  -- admin:audit:view
                                                                      (2, 4),  -- admin:admin:view
                                                                      (2, 6);  -- admin:log:view

-- ============================================================
-- 初始化完成
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;