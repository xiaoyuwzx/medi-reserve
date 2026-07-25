package com.medireserve.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在需要记录操作日志的方法上，AOP 切面将自动记录
 *
 * 使用示例：
 *   @LogOperation(module = "审核管理", operation = "审核通过医生")
 *   public Result<String> approve(Long doctorId) { ... }
 *
 * 注解属性说明：
 * - module：功能模块名称（如 "审核管理"、"管理员管理"）
 * - operation：具体操作描述（如 "审核通过医生"、"禁用管理员"）
 * - recordParams：是否记录请求参数（默认 true，对于包含密码的接口设为 false）
 * - recordResult：是否记录响应结果（默认 false，避免大对象序列化）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /**
     * 操作模块名称（如："审核管理", "管理员管理", "医生管理", "登录认证"）
     */
    String module();

    /**
     * 操作描述（如："审核通过医生", "禁用管理员"）
     */
    String operation();

    /**
     * 是否记录请求参数（默认 true，对于包含敏感信息的接口可设为 false）
     */
    boolean recordParams() default true;

    /**
     * 是否记录响应结果（默认 true，对于返回大数据量的接口可设为 false）
     */
    boolean recordResult() default false;
}