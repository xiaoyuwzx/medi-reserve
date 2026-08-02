package com.medireserve.common.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medireserve.common.dto.OssStsVO;
import com.medireserve.common.exception.SystemException;
import com.medireserve.common.properties.OssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * OSS STS 临时凭证服务（经典 V1 客户端）
 * 负责调用阿里云 STS API，通过 AssumeRole 获取临时安全凭证
 *
 * 核心职责：
 * 1. 调用阿里云 STS AssumeRole API 获取临时安全凭证
 * 2. 构建最小权限 Policy（限制上传目录）
 * 3. 封装返回给前端的 VO（含临时凭证 + 上传路径）
 *
 * 安全设计亮点：
 * - 权限隔离：每个医生只能上传到自己的目录（medi/doctor/{doctorId}/）
 * - 时效性：临时凭证 30 分钟过期，降低泄露风险
 * - 最小权限：只授予 oss:PutObject 权限，无法下载、删除或列举
 * - 后端校验：前端无法伪造 doctorId（从 JWT 中提取）
 */
@Slf4j
@Service
public class OssStsService {

    private final OssProperties ossProperties;

    /**
     * 构造器注入（推荐，避免字段注入警告）
     */
    public OssStsService(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    /**
     * 为指定医生生成 OSS 直传的 STS 临时凭证
     * @param doctorId 当前登录医生的 ID（用于隔离目录，防越权）
     * @return OssStsVO 包含临时 AK、SK、Token 及上传路径
     */
    public OssStsVO getStsCredential(Long doctorId) {
        try {
            // 1. 构建 STS 客户端
            // 注意：regionId 可以填 cn-hangzhou，或者从配置读取（但 STS 一般不要求特定 region）
            String regionId = "cn-hangzhou";
            // 创建 Profile 对象，指定 STS 地域和子账号 AK/SK
            IClientProfile profile = DefaultProfile.getProfile(
                    regionId,
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
            // 设置 STS Endpoint（默认就是 sts.aliyuncs.com，也可以不设）
            DefaultProfile.addEndpoint(regionId, "Sts", ossProperties.getStsEndpoint());

            IAcsClient client = new DefaultAcsClient(profile);

            // 2. 构建 AssumeRole 请求
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setMethod(MethodType.POST);                         // 必须 POST
            request.setRoleArn(ossProperties.getRoleArn());             // RAM 角色 ARN
            request.setRoleSessionName("doctor-" + doctorId);           // 会话名称（便于审计）
            request.setDurationSeconds(ossProperties.getTimeout());     // 有效期（秒）

            // 【核心】设置最小权限策略（防止越权）
            request.setPolicy(buildCustomPolicy(doctorId));

            // 3. 调用 STS 接口
            AssumeRoleResponse response = client.getAcsResponse(request);

            // 4. 提取临时凭证
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            // 5. 构建返回对象
            String dir = ossProperties.getBaseDir() + "/doctor/" + doctorId + "/";

            return OssStsVO.builder()
                    .accessKeyId(credentials.getAccessKeyId())           // 临时 AK
                    .accessKeySecret(credentials.getAccessKeySecret())   // 临时 SK
                    .securityToken(credentials.getSecurityToken())       // 临时 Token
                    .expiration(credentials.getExpiration())             // 过期时间
                    .bucket(ossProperties.getBucket())                   // Bucket 名称
                    .endpoint(ossProperties.getEndpoint())               // OSS 端点
                    .dir(dir)                                            // 上传目录
                    .build();

        } catch (Exception e) {
            log.error("获取 OSS STS 临时凭证失败，医生ID：{}，错误信息：{}", doctorId, e.getMessage(), e);
            throw new SystemException("获取阿里云上传凭证失败，请检查网络或联系管理员");
        }
    }

    /**
     * 构建最小权限的 RAM Policy（JSON 字符串）
     * 只允许用户上传到自己的目录下
     *
     * 权限限制：
     * - 只允许上传（oss:PutObject）
     * - 只能上传到指定目录（不允许跨目录）
     * - 不允许下载（oss:GetObject）、删除（oss:DeleteObject）、列举（oss:ListObjects）
     *
     * 安全效果：
     * - 前端拿到的临时凭证只能上传到 medi/doctor/123/ 目录
     * - 即使前端篡改上传路径，OSS 会校验 Policy，拒绝非法请求
     * - 医生 123 无法访问医生 456 的文件
     *
     * @param doctorId 医生 ID（用于构造资源路径）
     * @return Policy JSON 字符串
     */
    private String buildCustomPolicy(Long doctorId) {
        // 资源路径示例：acs:oss:oss-cn-wuhan-lr:medi-reserve-files/medi/doctor/1/*
        String resource = "acs:oss:*:*:" + ossProperties.getBucket()
                + "/" + ossProperties.getBaseDir()
                + "/doctor/" + doctorId + "/*";

        // ====== 2. 构建 Policy 结构 ======
        Map<String, Object> policy = new HashMap<>();
        policy.put("Version", "1");

        // Statement：权限声明
        Map<String, Object> statement = new HashMap<>();
        statement.put("Effect", "Allow");
        statement.put("Action", new String[]{"oss:PutObject"}); // 只允许上传
        statement.put("Resource", new String[]{resource});      // 只允许该路径

        policy.put("Statement", new Object[]{statement});

        // ====== 3. 序列化为 JSON ======
        try {
            return new ObjectMapper().writeValueAsString(policy);
        } catch (JsonProcessingException e) {
            log.error("构建 OSS Policy 失败", e);
            throw new SystemException("系统内部策略配置错误");
        }
    }
}