package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员端 - 医生账号查询 DTO
 */
@Data
@Schema(description = "医生账号查询 DTO（管理端）")
public class AdminDoctorQueryDTO {

    @Schema(description = "关键词（姓名/手机号模糊搜索）")
    private String keyword;

    @Schema(description = "科室ID")
    private Long departmentId;

    @Schema(description = "账号状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "审核状态：0-待审核，1-已通过，2-已驳回")
    private Integer auditStatus;

    @Schema(description = "页码（从1开始）")
    private Integer page = 1;

    @Schema(description = "每页条数（1-100）")
    private Integer size = 10;
}