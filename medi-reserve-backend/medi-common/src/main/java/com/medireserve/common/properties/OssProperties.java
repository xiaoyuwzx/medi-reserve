package com.medireserve.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS / STS 配置属性类
 * 对应 application.yml 中的 aliyun.oss 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    // OSS 访问端点（如 oss-cn-wuhan-lr.aliyuncs.com）
    private String endpoint;
    // 存储空间名称
    private String bucket;
    // STS 服务的访问端点（固定为 sts.aliyuncs.com）
    private String stsEndpoint;
    // 具备 AssumeRole 权限的 RAM 子账号 AccessKeyId
    private String accessKeyId;
    // 具备 AssumeRole 权限的 RAM 子账号 AccessKeySecret
    private String accessKeySecret;
    // RAM 角色的全局资源名称（ARN）
    private String roleArn;
    // 临时凭证有效期（单位：秒）
    private Long timeout;
    // 基础上传目录（例如 "medi"），会在拼接路径时使用
    private String baseDir;
}