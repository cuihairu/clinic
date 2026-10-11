package com.sinomed.service;

import com.sinomed.vo.StaffWeekTimesheetView;

/**
 * 周班次对照：计划班次（员工班表按星期）与实际打卡（signs）逐日对照
 */
public interface StaffWeekTimesheetService {

    /**
     * 取包含 weekDate 的整周（周一至周日）对照；weekDate 为空取当周，格式非法报 400。
     */
    StaffWeekTimesheetView weekTimesheet(String weekDate);
}
