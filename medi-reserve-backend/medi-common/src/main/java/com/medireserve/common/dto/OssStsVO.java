package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * OSS STS 临时凭证返回对象
 * 用于前端直传 OSS 时所需的全部参数
 */
@Data
@Builder
@Schema(description = "OSS STS 临时凭证返回对象")
public class OssStsVO {

    @Schema(description = "临时 AccessKeyId")
    private String accessKeyId;

    @Schema(description = "临时 AccessKeySecret")
    private String accessKeySecret;

    @Schema(description = "安全令牌（STS）")
    private String securityToken;

    @Schema(description = "凭证过期时间（ISO 8601）")
    private String expiration;

    @Schema(description = "OSS 存储空间名称")
    private String bucket;

    @Schema(description = "OSS 访问端点")
    private String endpoint;

    @Schema(description = "上传目标目录（需前端拼接）")
    private String dir;
}