package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理员端 - 医生账号管理返回 VO
 */
@Data
@Schema(description = "医生账号管理 VO（管理端）")
public class AdminDoctorVO {

    @Schema(description = "医生ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    @Schema(description = "科室ID")
    private Long departmentId;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "职称ID")
    private Long titleId;

    @Schema(description = "职称名称")
    private String titleName;

    @Schema(description = "擅长领域")
    private String specialty;

    @Schema(description = "个人简介")
    private String introduction;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "账号状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "审核状态：0-待审核，1-已通过，2-已驳回")
    private Integer auditStatus;

    @Schema(description = "注册时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}