package com.medireserve.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 对应数据库表 operation_log
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