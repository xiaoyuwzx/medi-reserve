package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志响应 VO
 */
@Data
@Schema(description = "操作日志响应 VO")
public class OperationLogVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "管理员ID")
    private Long adminId;

    @Schema(description = "管理员姓名")
    private String adminName;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作描述")
    private String operation;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求路径")
    private String path;

    @Schema(description = "请求参数（JSON）")
    private String params;

    @Schema(description = "客户端IP")
    private String ip;

    @Schema(description = "操作结果：1-成功，0-失败")
    private Integer result;

    @Schema(description = "HTTP状态码")
    private Integer statusCode;

    @Schema(description = "错误信息（失败时）")
    private String errorMsg;

    @Schema(description = "操作耗时（毫秒）")
    private Integer durationMs;

    @Schema(description = "操作时间")
    private LocalDateTime createdAt;
}