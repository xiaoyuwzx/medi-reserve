package com.medireserve.patient.controller;

import com.github.pagehelper.PageInfo;
import com.medireserve.common.annotation.LogOperation;
import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.MessageConstant;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.DoctorHotVO;
import com.medireserve.common.dto.EvaluationCreateDTO;
import com.medireserve.common.dto.EvaluationListVO;
import com.medireserve.common.dto.MyEvaluationVO;
import com.medireserve.common.entity.Evaluation;
import com.medireserve.common.result.Result;
import com.medireserve.patient.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 就诊评价控制器
 * 提供评价的增删改查及热门排行榜功能
 */
@Slf4j
@RestController
@RequestMapping("/patient")
@Tag(name = "患者端 - 就诊评价", description = "评价管理、热门医生排行")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 创建评价
     * 患者对已就诊的预约进行评价
     * @param createDTO
     * @param patientId
     * @return
     */
    @PostMapping("/evaluations")
    @RequireRole(RoleConstant.PATIENT)
    @LogOperation(module = "就诊评价", operation = "创建评价")
    @Operation(summary = "创建评价", description = "患者对已就诊的预约进行评分和文字评价")
    public Result<Map<String, Object>> createEvaluation(
            @RequestBody @Valid EvaluationCreateDTO createDTO,
            @RequestAttribute("userId") Long patientId){

        log.info("创建评价请求，患者ID：{}，预约ID：{}", patientId, createDTO.getAppointmentId());

        Evaluation evaluation = evaluationService.createEvaluation(patientId, createDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("evaluationId", evaluation.getId());
        map.put("doctorId", evaluation.getDoctorId());
        map.put("score", evaluation.getScore());
        map.put("createdAt", evaluation.getCreatedAt());

        log.info("创建评价成功，评价ID：{}", evaluation.getId());

        return Result.success(MessageConstant.EVALUATION_CREATE_SUCCESS, map);

    }

    /**
     * 查询我的评价列表(分页)
     * 患者查看自己提交的所有评价
     * @param page
     * @param size
     * @param patientId
     * @return
     */
    @GetMapping("/my-evaluations")
    @RequireRole(RoleConstant.PATIENT)
    @Operation(summary = "查询我的评价", description = "分页查询当前患者提交的所有评价")
    public Result<PageInfo<MyEvaluationVO>> getMyEvaluations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute("userId") Long patientId){

        log.info("查询我的评价，患者ID：{}，页码：{}，每页：{}", patientId, page, size);

        //校验参数
        if(page < 1) page = 1;
        if(size < 1 || size > 100) size = 10;

        PageInfo<MyEvaluationVO> pageInfo = evaluationService.getMyEvaluations(patientId, page, size);

        return Result.success(pageInfo);

    }

    /**
     * 查询医生的评价列表(公开访问)
     * @param doctorId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/doctors/{doctorId}/evaluations")
    @Operation(summary = "查询医生评价列表", description = "分页查询某医生的历史评价（公开接口，无需登录）")
    public Result<PageInfo<EvaluationListVO>> getDoctorEvaluations(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size){

        log.info("查询医生评价列表，医生ID：{}，页码：{}，每页：{}", doctorId, page, size);

        //校验数据
        if(page < 1) page = 1;
        if(size < 1 || size > 100) size = 10;

        PageInfo<EvaluationListVO> pageInfo = evaluationService.getDoctorEvaluations(doctorId, page, size);

        return Result.success(pageInfo);

    }

    /**
     * 删除评价(软删除)
     * 患者只能删除自己发布的评价
     * @param evaluationId
     * @param patientId
     * @return
     */
    @DeleteMapping("/evaluations/{evaluationId}")
    @RequireRole(RoleConstant.PATIENT)
    @LogOperation(module = "就诊评价", operation = "删除评价")
    @Operation(summary = "删除评价", description = "患者软删除自己提交的评价(状态改为隐藏)")
    public Result<String> deleteEvaluation(
            @PathVariable Long evaluationId,
            @RequestAttribute("userId") Long patientId){

        log.info("删除评价请求，评价ID：{}，患者ID：{}", evaluationId, patientId);

        evaluationService.deleteEvaluation(evaluationId, patientId);

        return Result.success(MessageConstant.EVALUATION_DELETE_SUCCESS);

    }

    /**
     * 获取热门医生排行榜(公开访问)
     * @return
     */
    @GetMapping("/doctors/hot")
    @Operation(summary = "热门医生排行榜", description = "获取热度前10的医生(基于近30天评价，时间衰败算法)")
    public Result<List<DoctorHotVO>> getHotDoctors(){

        log.info("获取热门医生排行榜");

        List<DoctorHotVO> hotList = evaluationService.getHotDoctors();

        return Result.success(hotList);

    }

    /**
     * 手动刷新热门医生缓存(管理员专用)
     * @return
     */
    @PostMapping("/admin/refresh-hot-cache")
    @RequireRole(RoleConstant.SUPER_ADMIN)
    @Operation(summary = "刷新热门医生缓存", description = "手动刷新热门医生排行榜(仅超级管理员)")
    public Result<String> refreshHotCache(){

        log.info("手动刷新热门医生缓存");

        evaluationService.refreshHotDoctorCache();

        return Result.success(MessageConstant.CACHE_REFRESH_SUCCESS);

    }

}
