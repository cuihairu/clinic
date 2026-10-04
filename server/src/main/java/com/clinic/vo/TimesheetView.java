package com.clinic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(title = "打卡记录")
@Builder
@Data
public class TimesheetView {
    String name;
    Integer signType;
    String time;
}
