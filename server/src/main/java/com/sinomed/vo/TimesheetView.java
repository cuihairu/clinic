package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(title = "打卡记录")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TimesheetView {
    String name;
    Integer signType;
    String time;
}
