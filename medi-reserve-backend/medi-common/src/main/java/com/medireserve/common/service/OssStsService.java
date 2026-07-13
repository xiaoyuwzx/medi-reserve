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
            request.setMethod(MethodType.POST);   // 必须 POST
            request.setRoleArn(ossProperties.getRoleArn());
            request.setRoleSessionName("doctor-" + doctorId);
            request.setDurationSeconds(ossProperties.getTimeout());
            // 【核心】设置最小权限策略（防止越权）
            request.setPolicy(buildCustomPolicy(doctorId));

            // 3. 调用 STS 接口
            AssumeRoleResponse response = client.getAcsResponse(request);

            // 4. 提取临时凭证
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            // 5. 构建返回对象
            String dir = ossProperties.getBaseDir() + "/doctor/" + doctorId + "/";

            return OssStsVO.builder()
                    .accessKeyId(credentials.getAccessKeyId())
                    .accessKeySecret(credentials.getAccessKeySecret())
                    .securityToken(credentials.getSecurityToken())
                    .expiration(credentials.getExpiration())
                    .bucket(ossProperties.getBucket())
                    .endpoint(ossProperties.getEndpoint())
                    .dir(dir)
                    .build();

        } catch (Exception e) {
            log.error("获取 OSS STS 临时凭证失败，医生ID：{}，错误信息：{}", doctorId, e.getMessage(), e);
            throw new SystemException("获取阿里云上传凭证失败，请检查网络或联系管理员");
        }
    }

    /**
     * 构建最小权限的 RAM Policy（JSON 字符串）
     * 只允许用户上传到自己的目录下
     */
    private String buildCustomPolicy(Long doctorId) {
        // 资源路径示例：acs:oss:oss-cn-hangzhou.aliyuncs.com:medi-reserve-files/medi/doctor/123/*
        String resource = "acs:oss:" + ossProperties.getEndpoint() + ":"
                + ossProperties.getBucket() + "/"
                + ossProperties.getBaseDir() + "/doctor/" + doctorId + "/*";

        Map<String, Object> policy = new HashMap<>();
        policy.put("Version", "1");

        Map<String, Object> statement = new HashMap<>();
        statement.put("Effect", "Allow");
        statement.put("Action", new String[]{"oss:PutObject"});
        statement.put("Resource", new String[]{resource});

        policy.put("Statement", new Object[]{statement});

        try {
            return new ObjectMapper().writeValueAsString(policy);
        } catch (JsonProcessingException e) {
            log.error("构建 OSS Policy 失败", e);
            throw new SystemException("系统内部策略配置错误");
        }
    }
}