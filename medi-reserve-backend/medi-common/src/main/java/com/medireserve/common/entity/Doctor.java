package com.medireserve.common.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Doctor {
    private Long id;    //医生ID
    private String name;    //姓名
    private String phone;   //手机号（登录账号）
    private String password;    //密码（BCrypt加密）
    private String idCard;  //身份证号
    private Integer gender; //性别：0未知 1男 2女
    private LocalDate birthDate;    //出生日期
    private String department;  //科室
    private String title;   //职称：主任医师/副主任医师/主治医师/住院医师
    private Integer status;    // 状态：0待审核 1审核通过(正常) 2审核驳回 3禁用
    private LocalDateTime createdAt;    //注册时间
    private LocalDateTime updatedAt;    //更新时间
}