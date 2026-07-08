package com.medireserve.admin.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.admin.service.AdminAuditService;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.AuditRejectDTO;
import com.medireserve.common.dto.DoctorPendingVO;
import com.medireserve.common.entity.DoctorAudit;
import com.medireserve.common.exception.PermissionDeniedException;
import com.medireserve.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
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
    AdminAuditService adminAuditService;

    /**
     * 分页查询待审核医生列表
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/doctors/pending")
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
    @Operation(summary = "审核通过", description = "管理员审核医生注册申请(仅限超级管理员)")
    public Result<String> approve(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentAdminId,
            @RequestAttribute("role") String currentRole){

        log.info("审核通过请求，医生ID：{}，操作人：{}，角色：{}", id, currentAdminId, currentRole);

        //权限校验(仅超级管理员)
        if(!RoleConstant.SUPER_ADMIN.equals(currentRole)){
            log.warn("权限不足，当前角色：{}，需要：{}", currentRole, RoleConstant.SUPER_ADMIN);
            throw new PermissionDeniedException("只有超级管理员才能执行审核操作");
        }

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
    @Operation(summary = "审核驳回", description = "管理员审核驳回医生注册申请，需填写驳回原因(仅限超级管理员)")
    public Result<String> reject(
            @PathVariable Long id,
            @RequestBody @Valid AuditRejectDTO rejectDTO,
            @RequestAttribute("userId") Long currentAdminId,
            @RequestAttribute("role") String currentRole){

        log.info("审核驳回请求，医生ID：{}，操作人：{}，角色：{}，驳回原因：{}", id, currentAdminId, currentRole, rejectDTO.getRejectReason());

        //权限校验(仅超级管理员)
        if(!RoleConstant.SUPER_ADMIN.equals(currentRole)){
            log.warn("权限不足，当前角色：{}，需要：{}", currentRole, RoleConstant.SUPER_ADMIN);
            throw new PermissionDeniedException("只有超级管理员才能执行审核操作");
        }

        adminAuditService.reject(id, currentAdminId, rejectDTO.getRejectReason());

        return Result.success(MessageConstant.DOCTOR_AUDIT_REJECT_SUCCESS);

    }

}
