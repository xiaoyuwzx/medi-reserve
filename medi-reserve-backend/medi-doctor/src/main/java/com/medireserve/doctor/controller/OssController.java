package com.medireserve.doctor.controller;

import com.medireserve.common.annotation.RequireRole;
import com.medireserve.common.constant.RoleConstant;
import com.medireserve.common.dto.OssStsVO;
import com.medireserve.common.result.Result;
import com.medireserve.common.service.OssStsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 阿里云 OSS 文件上传控制器（医生端）
 * 提供 STS 临时凭证供前端直传
 */
@Slf4j
@RestController
@RequestMapping("/doctor/oss")
@Tag(name = "医生端 - 文件上传", description = "获取 OSS STS 临时凭证")
public class OssController {

    private OssStsService ossStsService;

    public OssController(OssStsService ossStsService) {
        this.ossStsService = ossStsService;
    }

    /**
     * 获取 OSS STS 临时凭证
     * 前端根据返回的凭证和目录，直接上传文件到阿里云 OSS
     * @param doctorId 当前登录医生的 ID（从 JWT 中提取）
     * @return 包含临时 AK/SK/Token 及上传路径的凭证对象
     */
    @GetMapping({"/sts-token", "/sts"})
    @RequireRole(RoleConstant.DOCTOR)
    @Operation(summary = "获取 OSS 上传凭证", description = "返回 STS 临时凭证，供前端直传文件使用（有效期30分钟）")
    public Result<OssStsVO> getStsToken(@RequestAttribute("userId") Long doctorId) {
        log.info("医生 {} 请求获取 OSS 上传凭证", doctorId);
        OssStsVO vo = ossStsService.getStsCredential(doctorId);
        return Result.success(vo);
    }
}