package com.clinic.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Schema(title = "今天打卡记录")
@Builder
@Data
public class TimesheetDayView {
    Long total;// 总计多少小时
    Integer day;
    String  startTime;
    String  endTime;
    List<TimesheetView> data;
}
