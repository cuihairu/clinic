package com.clinic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(title = "一天考勤记录")
@Builder
@Data
public class TimesheetStaffView {
    Long staffId;
    String name;
    Long total;
    List<TimesheetDayView> data;
}
