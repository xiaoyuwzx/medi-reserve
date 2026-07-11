package com.medireserve.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Title {
    private Long id;
    private String name;    // 职称名称
    private Integer sortOrder;  // 排序编号
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}