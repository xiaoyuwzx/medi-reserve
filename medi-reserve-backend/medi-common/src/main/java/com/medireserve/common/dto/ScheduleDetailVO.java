package com.medireserve.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班详情返回对象（挂号确认页展示）
 * GET /patient/schedules/{scheduleId} 的返回数据
 */
@Data
@Schema(description = "排班详情返回 VO（挂号确认页）")
public class ScheduleDetailVO {

    @Schema(description = "排班ID")
    private Long scheduleId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "职称名称")
    private String titleName;

    @Schema(description = "排班日期")
    private LocalDate scheduleDate;

    @Schema(description = "时段：1=上午，2=下午")
    private Integer period;

    @Schema(description = "时段文本（上午/下午）")
    private String periodText;

    @Schema(description = "剩余号源")
    private Integer remainingCount;

    @Schema(description = "排班状态：1-正常，2-停诊，3-已满")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;
}