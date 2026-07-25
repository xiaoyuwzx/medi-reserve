package com.medireserve.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 对应数据库表 operation_log
 *
 * 字段说明：
 * - admin_id / admin_name：操作人（冗余存储，即使管理员被删除也有记录）
 * - module / operation：操作分类和描述
 * - method / path：HTTP 方法 + 路径
 * - params：请求参数（JSON 格式，已脱敏）
 * - ip：客户端 IP（用于安全审计）
 * - result：1 成功，0 失败
 * - duration_ms：操作耗时（用于性能监控）
 */
@Data
public class OperationLog {
    private Long id;
    private Long adminId;
    private String adminName;
    private String module;
    private String operation;
    private String method;
    private String path;
    private String params;
    private String ip;
    private Integer result;     // 1成功 0失败
    private Integer statusCode;
    private String errorMsg;
    private Integer durationMs;
    private LocalDateTime createdAt;
}