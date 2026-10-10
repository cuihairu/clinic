package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 员工请假视图：字段与 staff_leaves 表对齐；staffName 为列表联出展示字段。
 * 口径：请假为记录性质（类型/事由/起止时间），不改员工登录状态、无审批流（演示口径）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "员工请假")
public class StaffLeaveView {
    @Schema(title = "记录id", example = "1")
    private Long id;

    @Schema(title = "员工id", example = "2")
    private Long staffId;

    @Schema(title = "员工名（列表联出）", example = "沈中医师")
    private String staffName;

    @Schema(title = "类型：0 病假 / 1 事假", example = "1")
    private Integer leaveType;

    @Schema(title = "事由", example = "家中急事")
    private String reason;

    @Schema(title = "起始时间")
    private Date startTime;

    @Schema(title = "结束时间")
    private Date endTime;

    @Schema(title = "提交时间")
    private Date createTime;
}
