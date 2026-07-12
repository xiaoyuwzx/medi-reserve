package com.medireserve.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Admin {
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String name;
    private String phone;
    private String email;
    private Integer role;   // 1超级管理员 2普通管理员
    private Integer status;
    private String lastLoginIp;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}