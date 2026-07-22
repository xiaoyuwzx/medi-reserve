package com.medireserve.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标注在需要特定权限的接口方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /**
     * 权限代码，如 "admin:audit:view"
     */
    String value();
}