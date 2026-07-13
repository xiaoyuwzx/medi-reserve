package com.medireserve.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Doctor {
    private Long id;    //医生ID
    private String name;    //姓名
    private String phone;   //手机号（登录账号）
    @JsonIgnore
    private String password;    //密码（BCrypt加密）
    private String idCard;  //身份证号
    private Integer gender; //性别：0未知 1男 2女
    private LocalDate birthDate;    //出生日期
    private Long departmentId;  //科室 (外键)
    private Long titleId;   //职称 (外键)

    private transient String departmentName;  // 瞬态
    private transient String titleName;       // 瞬态

    private Integer status;    // 状态：0禁用 1正常
    private LocalDateTime createdAt;    //注册时间
    private LocalDateTime updatedAt;    //更新时间
}