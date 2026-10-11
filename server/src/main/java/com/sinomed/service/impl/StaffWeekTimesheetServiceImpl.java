package com.sinomed.service.impl;

import com.sinomed.entity.SignEntity;
import com.sinomed.entity.StaffEntity;
import com.sinomed.entity.StaffShiftEntity;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.StaffService;
import com.sinomed.service.StaffShiftService;
import com.sinomed.service.StaffWeekTimesheetService;
import com.sinomed.util.DateUtil;
import com.sinomed.util.TimesheetUtil;
import com.sinomed.vo.StaffShiftView;
import com.sinomed.vo.StaffWeekTimesheetView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * 周班次对照实现：按包含入参日期的周一至周日取 signs，走与月度考勤同一套 TimesheetUtil
 * 统计口径（同日最早上班/最晚下班、整小时取整），再按星期对照该员工的周期班次。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffWeekTimesheetServiceImpl implements StaffWeekTimesheetService {

    private static final SimpleDateFormat DAY_FMT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm");
    private static final TimeZone SYSTEM = TimeZone.getDefault();

    static {
        DAY_FMT.setTimeZone(SYSTEM);
        TIME_FMT.setTimeZone(SYSTEM);
    }

    private final StaffService staffService;
    private final StaffRepository staffRepository;
    private final StaffShiftService shiftService;

    @Override
    public StaffWeekTimesheetView weekTimesheet(String weekDate) {
        Date anchor = resolveAnchor(weekDate);
        // Calendar.SUNDAY=1 … SATURDAY=7；归一到本周已过天数（周一 0 … 周日 6）后回退到周一
        Calendar cal = Calendar.getInstance();
        cal.setTime(anchor);
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int daysIntoWeek = (dow - Calendar.MONDAY + 7) % 7;
        Date monday = DateUtil.getZeroTime(DateUtil.getDateAfter(anchor, -daysIntoWeek));

        Date start = DateUtil.getZeroTime(monday);
        Date end = DateUtil.getTailTime(DateUtil.getDateAfter(monday, 6));
        Page<SignEntity> signs = staffService.findSignAllByPage(null,
                DateUtil.stdFormat(start), DateUtil.stdFormat(end), PageRequest.ofSize(Integer.MAX_VALUE));
        TimesheetUtil.TimesheetFilter filter = TimesheetUtil.TimesheetFilter.of(signs.toList());
        filter.statistic(new Date());
        Map<Long, Map<Date, TimesheetUtil.DayFilter>> byStaff = new HashMap<>();
        filter.getData().forEach((staffId, staffFilter) -> byStaff.put(staffId, staffFilter.getData()));

        List<StaffEntity> staffs = staffRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<StaffWeekTimesheetView.StaffWeekRow> rows = staffs.stream()
                .map(staff -> toRow(staff, monday, byStaff.get(staff.getId())))
                .toList();

        return StaffWeekTimesheetView.builder()
                .weekStart(DAY_FMT.format(monday))
                .staffs(rows)
                .build();
    }

    private StaffWeekTimesheetView.StaffWeekRow toRow(StaffEntity staff, Date monday,
                                                       Map<Date, TimesheetUtil.DayFilter> dayFilters) {
        Map<Integer, StaffShiftEntity> planByWeekday = new HashMap<>();
        shiftService.listByStaff(staff.getId()).forEach(shift -> planByWeekday.put(shift.getWeekday(), shift));

        List<StaffWeekTimesheetView.DayRow> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Date day = DateUtil.getDateAfter(monday, i);
            int weekday = i + 1;
            StaffShiftEntity plan = planByWeekday.get(weekday);
            TimesheetUtil.DayFilter actual = dayFilters == null ? null : dayFilters.get(DateUtil.getZeroTime(day));
            days.add(StaffWeekTimesheetView.DayRow.builder()
                    .date(DAY_FMT.format(day))
                    .weekday(weekday)
                    .weekdayText(StaffShiftView.weekdayText(weekday))
                    .planStart(plan == null ? null : plan.getStart())
                    .planEnd(plan == null ? null : plan.getEnd())
                    .signStart(shortTime(actual, false))
                    .signEnd(shortTime(actual, true))
                    .hours(actual == null ? null : actual.getHours())
                    .build());
        }

        return StaffWeekTimesheetView.StaffWeekRow.builder()
                .staffId(staff.getId())
                .name(staff.getName())
                .days(days)
                .build();
    }

    /** 打卡时刻转 HH:mm；取最早上班（start）或最晚下班（end），无打卡返回 null */
    private String shortTime(TimesheetUtil.DayFilter dayFilter, boolean end) {
        if (dayFilter == null) {
            return null;
        }
        Date time = end ? dayFilter.getEnd() : dayFilter.getStart();
        return time == null ? null : TIME_FMT.format(time);
    }

    /** 入参为空取当天；格式非法（yyyy-MM-dd / yyyy-MM-dd HH:mm:ss）报 400 */
    private Date resolveAnchor(String weekDate) {
        if (weekDate == null || weekDate.trim().isEmpty()) {
            return new Date();
        }
        Date parsed = DateUtil.parseParam(weekDate.trim());
        if (parsed == null) {
            throw new IllegalArgumentException("无效的周日期：" + weekDate + "（应为 yyyy-MM-dd）");
        }
        return parsed;
    }
}
