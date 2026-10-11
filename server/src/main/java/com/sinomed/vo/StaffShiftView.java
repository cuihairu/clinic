package com.sinomed.vo;

import com.sinomed.entity.StaffShiftEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 班次视图：字段与 staff_shifts 表对齐，联出员工姓名与星期文案。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "员工班次")
public class StaffShiftView {
    @Schema(title = "班次id", example = "1")
    private Long id;

    @Schema(title = "员工id", example = "2")
    private Long staffId;

    @Schema(title = "员工姓名", example = "沈知远")
    private String staffName;

    @Schema(title = "星期（1 周一 … 7 周日）", example = "3")
    private Integer weekday;

    @Schema(title = "星期文案", example = "周三")
    private String weekdayText;

    @Schema(title = "上班时间 HH:mm", example = "09:00")
    private String start;

    @Schema(title = "下班时间 HH:mm", example = "18:00")
    private String end;

    @Schema(title = "创建时间", example = "2026-09-25 09:00:00")
    private Date createTime;

    /** 星期文案：1 周一 … 7 周日，越界原样返回数字 */
    public static String weekdayText(Integer weekday) {
        if (weekday == null || weekday < 1 || weekday > 7) {
            return String.valueOf(weekday);
        }
        return new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"}[weekday - 1];
    }

    public static StaffShiftView FromEntity(StaffShiftEntity entity) {
        return FromEntity(entity, null);
    }

    public static StaffShiftView FromEntity(StaffShiftEntity entity, String staffName) {
        return StaffShiftView.builder()
                .id(entity.getId())
                .staffId(entity.getStaffId())
                .staffName(staffName)
                .weekday(entity.getWeekday())
                .weekdayText(weekdayText(entity.getWeekday()))
                .start(entity.getStart())
                .end(entity.getEnd())
                .createTime(entity.getCreateTime())
                .build();
    }
}
