package com.medireserve.admin.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.admin.service.AdminPatientManageService;
import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.AdminPatientQueryDTO;
import com.medireserve.common.dto.AdminPatientStatusDTO;
import com.medireserve.common.dto.AdminPatientVO;
import com.medireserve.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 患者账号管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/patients")
@Tag(name = "管理端 - 患者账号管理", description = "管理员管理患者账号")
@RequireRole({RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN})
public class AdminPatientManageController {

    @Autowired
    private AdminPatientManageService adminPatientManageService;

    /**
     * 分页查询患者账号列表
     */
    @GetMapping
    @Operation(summary = "查询患者账号列表", description = "分页查询患者账号，支持关键词、状态筛选")
    public Result<PageInfo<AdminPatientVO>> listPatients(@Valid AdminPatientQueryDTO query) {
        log.info("管理员查询患者账号列表，条件：{}", query);
        PageInfo<AdminPatientVO> pageInfo = adminPatientManageService.listPatients(query);
        return Result.success(pageInfo);
    }

    /**
     * 查询患者详情
     */
    @GetMapping("/{patientId}")
    @Operation(summary = "查询患者详情", description = "查看患者的完整信息")
    public Result<AdminPatientVO> getPatientDetail(@PathVariable Long patientId) {
        log.info("管理员查询患者详情，患者ID：{}", patientId);
        AdminPatientVO detail = adminPatientManageService.getPatientDetail(patientId);
        return Result.success(detail);
    }

    /**
     * 修改患者账号状态
     */
    @PatchMapping("/{patientId}/status")
    @LogOperation(module = "患者管理", operation = "修改患者账号状态")
    @Operation(summary = "修改患者账号状态", description = "启用或禁用患者账号")
    public Result<Void> updatePatientStatus(
            @PathVariable Long patientId,
            @RequestBody @Valid AdminPatientStatusDTO dto) {
        log.info("管理员修改患者账号状态，患者ID：{}，目标状态：{}", patientId, dto.getStatus());
        adminPatientManageService.updatePatientStatus(patientId, dto);
        return Result.success();
    }
}