package com.medireserve.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * OSS STS 临时凭证返回对象
 * 用于前端直传 OSS 时所需的全部参数
 */
@Data
@Builder
public class OssStsVO {
    // 临时 AccessKeyId（切记：非主账号 AK，仅 30 分钟有效）
    private String accessKeyId;
    // 临时 AccessKeySecret
    private String accessKeySecret;
    // 安全令牌（STS 的核心凭证，必须携带）
    private String securityToken;
    // 凭证过期时间（ISO 8601 格式，前端可用于倒计时提醒）
    private String expiration;
    // OSS 存储空间名称
    private String bucket;
    // OSS 访问端点
    private String endpoint;
    // 文件上传的目标目录（前端必须拼接此路径作为 objectName 前缀）
    private String dir;
}