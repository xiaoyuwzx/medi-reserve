-- =============================================
-- MediReserve 数据库初始化脚本
-- 在 MySQL 容器首次启动时自动执行
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 创建数据库（如果不存在）
-- =============================================
CREATE DATABASE IF NOT EXISTS `medi_reserve`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `medi_reserve`;

-- =============================================
-- 1. 科室字典表（department）
-- =============================================
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室ID',
                              `name` varchar(50) NOT NULL COMMENT '科室名称',
                              `sort_order` int DEFAULT 0 COMMENT '排序序号',
                              `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室字典表';

-- 初始化科室数据
INSERT INTO `department` (`name`, `sort_order`) VALUES
                                                    ('内科', 1),
                                                    ('外科', 2),
                                                    ('妇产科', 3),
                                                    ('儿科', 4),
                                                    ('骨科', 5),
                                                    ('眼科', 6),
                                                    ('耳鼻喉科', 7),
                                                    ('皮肤科', 8),
                                                    ('中医科', 9);

-- =============================================
-- 2. 职称字典表（title）
-- =============================================
DROP TABLE IF EXISTS `title`;
CREATE TABLE `title` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '职称ID',
                         `name` varchar(30) NOT NULL COMMENT '职称名称',
                         `sort_order` int DEFAULT 0 COMMENT '排序序号',
                         `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职称字典表';

-- 初始化职称数据
INSERT INTO `title` (`name`, `sort_order`) VALUES
                                               ('住院医师', 1),
                                               ('主治医师', 2),
                                               ('副主任医师', 3),
                                               ('主任医师', 4);

-- =============================================
-- 3. 管理员表（admin）
-- =============================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `username` varchar(50) NOT NULL COMMENT '用户名',
                         `password` varchar(100) NOT NULL COMMENT '密码（BCrypt）',
                         `name` varchar(50) NOT NULL COMMENT '姓名',
                         `phone` varchar(20) DEFAULT NULL,
                         `email` varchar(50) DEFAULT NULL,
                         `role` tinyint DEFAULT 2 COMMENT '1-超级管理员 2-普通管理员',
                         `status` tinyint DEFAULT 1 COMMENT '0-禁用 1-正常',
                         `last_login_ip` varchar(50) DEFAULT NULL,
                         `last_login_time` datetime DEFAULT NULL,
                         `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 初始化超级管理员（密码：Admin123）
-- BCrypt 加密后的 "Admin123"
INSERT INTO `admin` (`username`, `password`, `name`, `role`, `status`) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, 1);

-- =============================================
-- 4. 患者表（patient）
-- =============================================
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `name` varchar(50) NOT NULL,
                           `phone` varchar(20) NOT NULL COMMENT '手机号（登录账号）',
                           `password` varchar(100) NOT NULL COMMENT 'BCrypt加密',
                           `id_card` varchar(18) DEFAULT NULL,
                           `gender` tinyint DEFAULT 0 COMMENT '0-未知 1-男 2-女',
                           `status` tinyint DEFAULT 1 COMMENT '0-禁用 1-正常',
                           `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者表';

-- 初始化测试患者（密码：123456Aa）
INSERT INTO `patient` (`name`, `phone`, `password`, `status`) VALUES
    ('测试患者', '13900139001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1);

-- =============================================
-- 5. 医生表（doctor）
-- =============================================
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `name` varchar(50) NOT NULL,
                          `phone` varchar(20) NOT NULL COMMENT '手机号（登录账号）',
                          `password` varchar(100) NOT NULL,
                          `id_card` varchar(18) DEFAULT NULL,
                          `gender` tinyint DEFAULT 0,
                          `birth_date` date DEFAULT NULL,
                          `department_id` bigint DEFAULT NULL,
                          `title_id` bigint DEFAULT NULL,
                          `status` tinyint DEFAULT 1 COMMENT '0-禁用 1-正常',
                          `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `phone` (`phone`),
                          KEY `fk_doctor_department` (`department_id`),
                          KEY `fk_doctor_title` (`title_id`),
                          CONSTRAINT `fk_doctor_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT,
                          CONSTRAINT `fk_doctor_title` FOREIGN KEY (`title_id`) REFERENCES `title` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

-- 初始化测试医生（密码：123456Aa）
INSERT INTO `doctor` (`name`, `phone`, `password`, `department_id`, `title_id`, `status`) VALUES
    ('张主任', '13900138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, 4, 1);

-- =============================================
-- 6. 医生审核表（doctor_audit）
-- =============================================
DROP TABLE IF EXISTS `doctor_audit`;
CREATE TABLE `doctor_audit` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `doctor_id` bigint NOT NULL,
                                `certificate_url` varchar(255) DEFAULT NULL,
                                `qualification_url` varchar(255) DEFAULT NULL,
                                `specialty` varchar(255) DEFAULT NULL,
                                `introduction` text,
                                `avatar` varchar(255) DEFAULT NULL,
                                `audit_status` tinyint DEFAULT 0 COMMENT '0-待审核 1-通过 2-驳回',
                                `audit_remark` varchar(255) DEFAULT NULL,
                                `audit_time` datetime DEFAULT NULL,
                                `auditor_id` bigint DEFAULT NULL,
                                `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `doctor_id` (`doctor_id`),
                                KEY `idx_audit_status` (`audit_status`),
                                CONSTRAINT `doctor_audit_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生审核表';

-- 初始化医生审核数据（已通过）
INSERT INTO `doctor_audit` (`doctor_id`, `specialty`, `introduction`, `audit_status`, `audit_time`, `auditor_id`) VALUES
    (1, '心血管疾病、高血压、冠心病', '从事心内科临床工作15年', 1, NOW(), 1);

-- =============================================
-- 7. 排班表（schedule）
-- =============================================
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `doctor_id` bigint NOT NULL,
                            `schedule_date` date NOT NULL,
                            `period` tinyint NOT NULL COMMENT '1-上午 2-下午',
                            `max_count` int DEFAULT 20,
                            `remaining_count` int DEFAULT 20,
                            `status` tinyint DEFAULT 1 COMMENT '1-正常 2-停诊 3-已满',
                            `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_doctor_date_period` (`doctor_id`, `schedule_date`, `period`),
                            KEY `idx_doctor_id` (`doctor_id`),
                            KEY `idx_date` (`schedule_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

-- 初始化排班数据（未来7天，每天上午+下午）
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `period`, `max_count`, `remaining_count`, `status`) VALUES
                                                                                                              (1, CURDATE(), 1, 20, 15, 1),
                                                                                                              (1, CURDATE(), 2, 20, 18, 1),
                                                                                                              (1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 20, 20, 1),
                                                                                                              (1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 2, 20, 20, 1),
                                                                                                              (1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1, 20, 20, 1),
                                                                                                              (1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 20, 20, 1),
                                                                                                              (1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1, 20, 20, 1),
                                                                                                              (1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 2, 20, 20, 1);

-- =============================================
-- 8. 预约表（appointment）
-- =============================================
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `appointment_no` varchar(32) NOT NULL COMMENT '预约单号',
                               `schedule_id` bigint NOT NULL,
                               `patient_id` bigint NOT NULL,
                               `doctor_id` bigint NOT NULL,
                               `status` tinyint DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已完成 3-已取消 4-已过期',
                               `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `appointment_no` (`appointment_no`),
                               KEY `idx_schedule_id` (`schedule_id`),
                               KEY `idx_patient_id` (`patient_id`),
                               KEY `idx_doctor_id` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- =============================================
-- 9. 评价表（evaluation）
-- =============================================
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `appointment_id` bigint NOT NULL,
                              `patient_id` bigint NOT NULL,
                              `doctor_id` bigint NOT NULL,
                              `schedule_id` bigint NOT NULL,
                              `score` tinyint NOT NULL COMMENT '1-5',
                              `content` varchar(500) DEFAULT NULL,
                              `is_anonymous` tinyint DEFAULT 0 COMMENT '0-否 1-是',
                              `status` tinyint DEFAULT 1 COMMENT '1-已发布 2-已隐藏',
                              `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_appointment` (`appointment_id`),
                              KEY `idx_doctor_id` (`doctor_id`),
                              KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- =============================================
-- 10. 问诊消息表（consultation_message）
-- =============================================
DROP TABLE IF EXISTS `consultation_message`;
CREATE TABLE `consultation_message` (
                                        `id` bigint NOT NULL AUTO_INCREMENT,
                                        `appointment_id` bigint NOT NULL,
                                        `sender_id` bigint NOT NULL,
                                        `receiver_id` bigint NOT NULL,
                                        `sender_role` varchar(20) NOT NULL COMMENT 'PATIENT/DOCTOR',
                                        `content` varchar(1000) NOT NULL,
                                        `msg_type` tinyint DEFAULT 1 COMMENT '1-文本 2-图片',
                                        `send_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                        PRIMARY KEY (`id`),
                                        KEY `idx_appointment_id` (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊消息表';

-- =============================================
-- 完成
-- =============================================
SET FOREIGN_KEY_CHECKS = 1;