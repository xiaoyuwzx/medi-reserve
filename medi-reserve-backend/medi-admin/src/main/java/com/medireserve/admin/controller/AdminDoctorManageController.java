package com.medireserve.admin.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.admin.service.AdminDoctorManageService;
import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.AdminDoctorQueryDTO;
import com.medireserve.common.dto.AdminDoctorStatusDTO;
import com.medireserve.common.dto.AdminDoctorVO;
import com.medireserve.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端 - 医生账号管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/doctors")
@Tag(name = "管理端 - 医生账号管理", description = "管理员管理已审核通过的医生账号")
@RequireRole({RoleConstant.SUPER_ADMIN, RoleConstant.ADMIN})
public class AdminDoctorManageController {

    @Autowired
    private AdminDoctorManageService adminDoctorManageService;

    /**
     * 分页查询医生账号列表
     */
    @GetMapping
    @Operation(summary = "查询医生账号列表", description = "分页查询已审核通过的医生账号，支持关键词、科室、状态筛选")
    public Result<PageInfo<AdminDoctorVO>> listDoctors(@Valid AdminDoctorQueryDTO query) {
        log.info("管理员查询医生账号列表，条件：{}", query);
        PageInfo<AdminDoctorVO> pageInfo = adminDoctorManageService.listDoctors(query);
        return Result.success(pageInfo);
    }

    /**
     * 查询医生详情
     */
    @GetMapping("/{doctorId}")
    @Operation(summary = "查询医生详情", description = "查看医生的完整信息（含审核资料）")
    public Result<AdminDoctorVO> getDoctorDetail(@PathVariable Long doctorId) {
        log.info("管理员查询医生详情，医生ID：{}", doctorId);
        AdminDoctorVO detail = adminDoctorManageService.getDoctorDetail(doctorId);
        return Result.success(detail);
    }

    /**
     * 修改医生账号状态
     */
    @PatchMapping("/{doctorId}/status")
    @LogOperation(module = "医生管理", operation = "修改医生账号状态")
    @Operation(summary = "修改医生账号状态", description = "启用或禁用医生账号")
    public Result<Void> updateDoctorStatus(
            @PathVariable Long doctorId,
            @RequestBody @Valid AdminDoctorStatusDTO dto) {
        log.info("管理员修改医生账号状态，医生ID：{}，目标状态：{}", doctorId, dto.getStatus());
        adminDoctorManageService.updateDoctorStatus(doctorId, dto);
        return Result.success();
    }
}