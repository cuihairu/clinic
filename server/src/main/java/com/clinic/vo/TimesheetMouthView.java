package com.clinic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(title = "考勤月报")
@Builder
@Data
public class TimesheetMouthView {
    Integer mouth;
    List<TimesheetStaffView> data;
}
