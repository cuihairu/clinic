package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 周班次对照：某周的每员工 × 每天，计划班次（staff_shifts 按星期）对照实际打卡（signs）。
 * 口径与月度考勤一致——同日多张上班卡取最早、下班卡取最晚，时长按整小时计（不足 1 小时不计）。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "周班次对照")
public class StaffWeekTimesheetView {

    @Schema(title = "该周周一日期", example = "2026-10-05")
    private String weekStart;

    @Schema(title = "员工行（每人 7 天）")
    private List<StaffWeekRow> staffs;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(title = "员工周班次对照")
    public static class StaffWeekRow {
        @Schema(title = "员工id", example = "2")
        private Long staffId;

        @Schema(title = "员工姓名", example = "沈知远")
        private String name;

        @Schema(title = "每天班次对照（周一 ~ 周日）")
        private List<DayRow> days;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(title = "每日班次对照")
    public static class DayRow {
        @Schema(title = "日期", example = "2026-10-05")
        private String date;

        @Schema(title = "星期（1 周一 … 7 周日）", example = "1")
        private Integer weekday;

        @Schema(title = "星期文案", example = "周一")
        private String weekdayText;

        @Schema(title = "计划上班时间 HH:mm（无班次为空）", example = "09:00")
        private String planStart;

        @Schema(title = "计划下班时间 HH:mm", example = "18:00")
        private String planEnd;

        @Schema(title = "实际上班打卡 HH:mm（无打卡为空）", example = "09:05")
        private String signStart;

        @Schema(title = "实际下班打卡 HH:mm", example = "17:55")
        private String signEnd;

        @Schema(title = "实际在岗整小时数", example = "8")
        private Long hours;
    }
}
