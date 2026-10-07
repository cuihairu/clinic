package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(title = "考勤月报")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TimesheetMouthView {
    Integer mouth;
    List<TimesheetStaffView> data;
}
