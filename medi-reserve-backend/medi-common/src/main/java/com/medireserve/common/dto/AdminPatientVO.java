package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员端 - 患者账号管理返回 VO
 */
@Data
@Schema(description = "患者账号管理 VO（管理端）")
public class AdminPatientVO {

    @Schema(description = "患者ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    @Schema(description = "账号状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "注册时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}