package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(title = "今天打卡记录")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TimesheetDayView {
    Long total;// 总计多少小时
    Integer day;
    String  startTime;
    String  endTime;
    List<TimesheetView> data;
}
