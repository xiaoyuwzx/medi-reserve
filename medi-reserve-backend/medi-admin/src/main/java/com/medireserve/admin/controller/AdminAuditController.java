package com.medireserve.admin.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.admin.service.AdminAuditService;
import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.annotation.RequirePermission;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.AuditRejectDTO;
import com.medireserve.common.dto.CertificateAuditDTO;
import com.medireserve.common.dto.DoctorPendingVO;
import com.medireserve.common.dto.PendingCertAuditVO;
import com.medireserve.common.entity.DoctorAudit;
import com.medireserve.common.exception.PermissionDeniedException;
import com.medireserve.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员：医生账号审核
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "管理端 - 医生审核", description = "管理员审核医生账号")
public class AdminAuditController {

    @Autowired
    private AdminAuditService adminAuditService;

    /**
     * 分页查询待审核医生列表
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/doctors/pending")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "查询待审核医生列表", description = "分页查询所有待审核的医生(按注册时间升序)")
    public Result<PageInfo<DoctorPendingVO>> listPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size){

        log.info("查询待审核医生列表，页码：{}，每页：{}", page, size);

        //参数校验
        if(page < 1) page = 1;
        if(size < 1 || size > 100) size = 10;

        //调用Service，PageHelper自动完成分页
        PageInfo<DoctorPendingVO> pageInfo = adminAuditService.listPending(page, size);

        return Result.success(pageInfo);

    }

    /**
     * 查看医生审核详细
     * @param id
     * @return
     */
    @GetMapping("/doctors/{id}/audit-detail")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "查看医生审核详细", description = "查看某位医生的完整注册信息和审核资料")
    public Result<DoctorAudit> getAuditDetail(@PathVariable Long id){

        log.info("查看医生审核详细，医生ID：{}", id);

        DoctorAudit doctorAudit = adminAuditService.getAuditDetail(id);

        return Result.success(doctorAudit);

    }

    /**
     * 审核通过
     * @param id
     * @param currentAdminId
     * @param currentRole
     * @return
     */
    @PatchMapping("/doctors/{id}/approve")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @LogOperation(module = "审核管理", operation = "审核通过医生")
    @Operation(summary = "审核通过", description = "管理员审核医生注册申请(仅限超级管理员)")
    public Result<String> approve(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentAdminId,
            @RequestAttribute("role") String currentRole){

        log.info("审核通过请求，医生ID：{}，操作人：{}，角色：{}", id, currentAdminId, currentRole);

        adminAuditService.approve(id, currentAdminId);

        return Result.success(MessageConstant.DOCTOR_AUDIT_APPROVE_SUCCESS);

    }

    /**
     * 审核驳回
     * @param id
     * @param rejectDTO
     * @param currentAdminId
     * @param currentRole
     * @return
     */
    @PatchMapping("/doctors/{id}/reject")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @LogOperation(module = "审核管理", operation = "审核驳回医生")
    @Operation(summary = "审核驳回", description = "管理员审核驳回医生注册申请，需填写驳回原因(仅限超级管理员)")
    public Result<String> reject(
            @PathVariable Long id,
            @RequestBody @Valid AuditRejectDTO rejectDTO,
            @RequestAttribute("userId") Long currentAdminId,
            @RequestAttribute("role") String currentRole){

        log.info("审核驳回请求，医生ID：{}，操作人：{}，角色：{}，驳回原因：{}", id, currentAdminId, currentRole, rejectDTO.getRejectReason());

        adminAuditService.reject(id, currentAdminId, rejectDTO.getRejectReason());

        return Result.success(MessageConstant.DOCTOR_AUDIT_REJECT_SUCCESS);

    }

    /**
     * 待审核证件列表（医生提交的证件变更申请）
     */
    @GetMapping("/cert-pending")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @RequirePermission("admin:audit:view")
    @Operation(summary = "待审核证件列表",
            description = "返回已提交证件变更申请的医生列表")
    public Result<PageInfo<PendingCertAuditVO>> listCertPending(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize) {
        log.info("查询待审核证件列表");
        PageInfo<PendingCertAuditVO> pageInfo = adminAuditService.listCertPending(pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 待审核证件详情
     */
    @GetMapping("/{doctorId}/cert-pending-detail")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @RequirePermission("admin:audit:view")
    @Operation(summary = "待审核证件详情")
    public Result<PendingCertAuditVO> getCertPendingDetail(
            @Parameter(description = "医生ID") @PathVariable Long doctorId) {
        log.info("查询待审核证件详情，医生ID：{}", doctorId);
        PendingCertAuditVO vo = adminAuditService.getCertPendingDetail(doctorId);
        return Result.success(vo);
    }

    /**
     * 审核医生证件变更
     */
    @PatchMapping("/{doctorId}/cert-audit")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @RequirePermission("admin:audit:approve")
    @Operation(summary = "审核医生证件变更",
            description = "通过：新证件生效；驳回：清空待审核数据，记录驳回原因")
    @LogOperation(module = "审核管理", operation = "审核医生证件变更")
    public Result<Void> auditCertificate(
            @Parameter(description = "医生ID") @PathVariable Long doctorId,
            @RequestAttribute("userId") Long adminId,
            @RequestBody @Valid CertificateAuditDTO dto) {
        log.info("审核医生证件变更，医生ID：{}，结果：{}", doctorId,
                dto.getResult() == 1 ? "通过" : "驳回");
        adminAuditService.auditCertificate(doctorId, adminId, dto);
        String msg = dto.getResult() == 1 ? "证件审核通过，已生效" : "证件审核驳回";
        return Result.success(msg, null);
    }

}
