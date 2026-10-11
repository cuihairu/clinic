package com.sinomed;

import com.sinomed.entity.SignEntity;
import com.sinomed.entity.StaffEntity;
import com.sinomed.repository.SignRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.StaffShiftService;
import com.sinomed.service.StaffWeekTimesheetService;
import com.sinomed.vo.StaffShiftView;
import com.sinomed.vo.StaffWeekTimesheetView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 周班次对照：周一锚定整周（周一起算 7 天），计划班次（staff_shifts 按星期）
 * 对照实际打卡（同日最早上班/最晚下班、整小时取整）；打卡时间用 SQL 回写绕开审计覆盖。
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:sqlite:target/staff-week-timesheet-test-${random.uuid}.db"})
@Transactional
class StaffWeekTimesheetTest {

    @Autowired
    private StaffWeekTimesheetService weekTimesheetService;

    @Autowired
    private StaffShiftService shiftService;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private SignRepository signRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private Long newStaff(String name) {
        StaffEntity staff = new StaffEntity();
        staff.setName(name);
        staff.setAccount("week-ts-" + System.nanoTime());
        staff.setPassword("x");
        staff.setStatus(1);
        return staffRepository.save(staff).getId();
    }

    private void shift(Long staffId, int weekday, String start, String end) {
        shiftService.save(StaffShiftView.builder().staffId(staffId).weekday(weekday).start(start).end(end).build());
    }

    private Date at(String day, int hour, int minute) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(day));
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            return cal.getTime();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** type 1 上班 0 下班；@CreatedDate 会覆盖显式时间，落库后 SQL 回写并清一级缓存，避免托管实体盖掉回写值 */
    private void sign(Long staffId, String day, int hour, int minute, int type) {
        SignEntity sign = new SignEntity();
        sign.setStaffId(staffId);
        sign.setType(type);
        Long id = signRepository.save(sign).getId();
        Timestamp time = new Timestamp(at(day, hour, minute).getTime());
        jdbcTemplate.update("UPDATE signs SET create_time = ?, update_time = ? WHERE id = ?", time, time, id);
        entityManager.clear();
    }

    @Test
    void weekRowsCoverPlansVersusSigns() {
        Long staffA = newStaff("甲");
        Long staffB = newStaff("乙");
        shift(staffA, 1, "09:00", "18:00");
        shift(staffA, 2, "09:30", "18:30");
        sign(staffA, "2026-10-05", 9, 5, 1);
        sign(staffA, "2026-10-05", 17, 55, 0);
        sign(staffA, "2026-10-06", 9, 31, 1); // 只上班无下班，历史日按 0 小时
        sign(staffB, "2026-10-07", 10, 0, 1);
        sign(staffB, "2026-10-07", 19, 0, 0);
        sign(staffB, "2026-10-12", 8, 0, 1); // 次周一，不应泄入本周

        StaffWeekTimesheetView view = weekTimesheetService.weekTimesheet("2026-10-07"); // 周三锚定周一
        assertEquals("2026-10-05", view.getWeekStart(), "周日期应锚定到周一");
        assertEquals(2, view.getStaffs().size(), "所有员工都要成行");
        assertEquals(staffA, view.getStaffs().get(0).getStaffId(), "员工按 id 升序");

        var daysA = view.getStaffs().get(0).getDays();
        assertEquals(7, daysA.size(), "每人固定 7 天");
        assertEquals("2026-10-05", daysA.get(0).getDate());
        assertEquals(1, daysA.get(0).getWeekday());
        assertEquals("周一", daysA.get(0).getWeekdayText());
        assertEquals("2026-10-11", daysA.get(6).getDate(), "周日收尾");
        assertEquals("周日", daysA.get(6).getWeekdayText());

        var mon = daysA.get(0);
        assertEquals("09:00", mon.getPlanStart());
        assertEquals("18:00", mon.getPlanEnd());
        assertEquals("09:05", mon.getSignStart(), "同日最早上班卡");
        assertEquals("17:55", mon.getSignEnd(), "同日最晚下班卡");
        assertEquals(8L, mon.getHours(), "8 小时 50 分整点取整为 8");

        var tue = daysA.get(1);
        assertEquals("09:30", tue.getPlanStart());
        assertEquals("18:30", tue.getPlanEnd());
        assertEquals("09:31", tue.getSignStart());
        assertNull(tue.getSignEnd(), "无下班卡应为空");
        assertEquals(0L, tue.getHours(), "历史日缺下班卡按 0 小时");

        var wed = daysA.get(2);
        assertNull(wed.getPlanStart(), "无班次的星期计划为空");
        assertNull(wed.getSignStart());

        var wedB = view.getStaffs().get(1).getDays().get(2);
        assertNull(wedB.getPlanStart(), "无班表员工计划为空");
        assertEquals("10:00", wedB.getSignStart());
        assertEquals("19:00", wedB.getSignEnd());
        assertEquals(9L, wedB.getHours());

        assertNull(view.getStaffs().get(1).getDays().get(6).getSignStart(), "下一周的打卡不泄入本周");
    }

    @Test
    void blankWeekDateUsesCurrentWeek() {
        Long staffA = newStaff("甲");
        shift(staffA, 1, "09:00", "18:00");

        for (String weekDate : new String[]{null, " ", ""}) {
            StaffWeekTimesheetView view = weekTimesheetService.weekTimesheet(weekDate);
            assertTrue(view.getWeekStart() != null && view.getWeekStart().matches("\\d{4}-\\d{2}-\\d{2}"),
                    "空入参应取当周：" + weekDate);
            assertEquals(1, view.getStaffs().size());
            assertEquals(7, view.getStaffs().get(0).getDays().size());
            assertEquals("09:00", view.getStaffs().get(0).getDays().get(0).getPlanStart(), "当周周一仍对照班表");
        }
    }

    @Test
    void invalidWeekDateGuard() {
        for (String bad : new String[]{"2026/10/07", "abc", "2026-13-40"}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> weekTimesheetService.weekTimesheet(bad), bad);
            assertTrue(e.getMessage().contains("无效的周日期"), e.getMessage());
        }
    }
}
