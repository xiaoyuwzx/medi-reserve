package com.medireserve.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();  // 允许的角色列表，如 {"PATIENT", "DOCTOR"}
}