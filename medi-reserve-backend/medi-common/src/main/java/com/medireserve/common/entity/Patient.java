package com.medireserve.common.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者实体类
 */
@Data
public class Patient {
    private Long id;    // 患者ID
    private String name;    //姓名
    private String phone;   // 手机号（登录账号）
    private String password;   // 密码（BCrypt加密）
    private String idCard;  // 身份证号
    private Integer gender; // 性别：0未知 1男 2女
    private LocalDate birthDate;    // 出生日期
    private String avatar;      // 头像URL
    private Integer status;     // 状态：0禁用 1正常
    private LocalDateTime createdAt;    //注册时间
    private LocalDateTime updatedAt;    // 更新时间
}