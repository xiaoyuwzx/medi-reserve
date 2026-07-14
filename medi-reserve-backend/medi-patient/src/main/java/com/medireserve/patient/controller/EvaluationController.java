package com.medireserve.patient.controller;

import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.EvaluationCreateDTO;
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

        // TODO : 新建评价成功常量
        return Result.success("评价成功", map);

    }

    

}
