package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(title = "一天考勤记录")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TimesheetStaffView {
    Long staffId;
    String name;
    Long total;
    List<TimesheetDayView> data;
}
