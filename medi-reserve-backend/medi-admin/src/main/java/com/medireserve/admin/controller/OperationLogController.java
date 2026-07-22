package com.medireserve.admin.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.admin.service.OperationLogService;
import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.OperationLogQueryDTO;
import com.medireserve.common.dto.OperationLogVO;
import com.medireserve.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志管理接口
 * 仅超级管理员可访问
 */
@Slf4j
@RestController
@RequestMapping("/admin/operation-logs")
@Tag(name = "管理端 - 操作日志", description = "查询和管理操作日志")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 分页查询日志列表
     * @param query
     * @return
     */
    @GetMapping
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "查询日志列表", description = "支持按操作人、模块、时间范围、结果状态筛选")
    public Result<PageInfo<OperationLogVO>> list(@Valid OperationLogQueryDTO query) {

        log.info("查询操作日志列表，条件：{}", query);

        PageInfo<OperationLogVO> pageInfo = operationLogService.findList(query);

        return Result.success(pageInfo);

    }

    /**
     * 查询日志详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "查看日志详情")
    public Result<OperationLogVO> detail(@Parameter(description = "日志ID") @PathVariable Long id) {

        log.info("查询操作日志详情，ID：{}", id);

        OperationLogVO vo = operationLogService.findById(id);

        return Result.success(vo);

    }

    /**
     * 删除日志
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "删除日志")
    @LogOperation(module = "日志管理", operation = "删除操作日志")
    public Result<Void> delete(@Parameter(description = "日志ID") @PathVariable Long id) {

        log.info("删除操作日志，ID：{}", id);

        operationLogService.deleteById(id);

        return Result.success("删除成功", null);

    }

}
