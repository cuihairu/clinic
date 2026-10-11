package com.sinomed;

import com.sinomed.entity.StaffEntity;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.StaffShiftService;
import com.sinomed.vo.StaffShiftView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 员工周期班表：员工 × 星期（1-7）× HH:mm 班次，同员工同星期唯一；
 * 时段校验（HH:mm 且 start < end）、员工/星期/班次存在性守卫、分页过滤分派。
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:sqlite:target/staff-shift-test-${random.uuid}.db"})
@Transactional
class StaffShiftServiceTest {

    @Autowired
    private StaffShiftService shiftService;

    @Autowired
    private StaffRepository staffRepository;

    /** 每个用例独立员工，避免方法执行顺序耦合 */
    private Long newStaff(String name) {
        StaffEntity staff = new StaffEntity();
        staff.setName(name);
        staff.setAccount("shift-" + System.nanoTime());
        staff.setPassword("x");
        staff.setStatus(1);
        return staffRepository.save(staff).getId();
    }

    private StaffShiftView view(Long staffId, Integer weekday, String start, String end) {
        return StaffShiftView.builder()
                .staffId(staffId).weekday(weekday).start(start).end(end).build();
    }

    @Test
    void saveAndFindJoinsStaffNameAndWeekdayText() {
        Long staffId = newStaff("沈知远");
        StaffShiftView saved = shiftService.findById(
                shiftService.save(view(staffId, 3, "09:00", "18:00")).getId());
        assertEquals(staffId, saved.getStaffId());
        assertEquals("沈知远", saved.getStaffName(), "详情应联出员工姓名");
        assertEquals(3, saved.getWeekday());
        assertEquals("周三", saved.getWeekdayText(), "星期应给中文文案");
        assertEquals("09:00", saved.getStart());
        assertEquals("18:00", saved.getEnd());
        assertTrue(saved.getCreateTime() != null, "创建时间由审计写入");
    }

    @Test
    void weekdayAndTimeGuards() {
        Long staffId = newStaff("苏文若");
        record Case(Long staffId, Integer weekday, String start, String end, String expect) {}
        List<Case> bad = List.of(
                new Case(staffId, null, "09:00", "18:00", "星期无效"),
                new Case(staffId, 0, "09:00", "18:00", "星期无效"),
                new Case(staffId, 8, "09:00", "18:00", "星期无效"),
                new Case(staffId, 1, "9:00", "18:00", "开始时间格式无效"),
                new Case(staffId, 1, "0900", "18:00", "开始时间格式无效"),
                new Case(staffId, 1, "09:00", "18:60", "结束时间格式无效"),
                new Case(staffId, 1, "25:00", "26:00", "开始时间格式无效"),
                new Case(staffId, 1, "18:00", "09:00", "结束时间须晚于开始时间"),
                new Case(staffId, 1, "09:00", "09:00", "结束时间须晚于开始时间"));
        for (Case c : bad) {
            try {
                shiftService.save(view(c.staffId(), c.weekday(), c.start(), c.end()));
                throw new AssertionError("应报 400：" + c.expect());
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains(c.expect()), c.expect() + " ← " + e.getMessage());
            }
        }
        try {
            shiftService.save(view(9999L, 1, "09:00", "18:00"));
            throw new AssertionError("未知员工应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("员工不存在"));
        }
    }

    @Test
    void duplicateGuardAllowsSelfOnUpdate() {
        Long staffId = newStaff("顾景明");
        Long wedId = shiftService.save(view(staffId, 3, "09:00", "18:00")).getId();
        try {
            shiftService.save(view(staffId, 3, "10:00", "19:00"));
            throw new AssertionError("同员工同星期重复应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("员工周三已有班次"));
        }
        // 其他员工同星期不受限
        Long otherId = newStaff("沈知远");
        shiftService.save(view(otherId, 3, "09:00", "18:00"));
        // 更新放过自身（同员工同星期仍是自己）
        shiftService.update(StaffShiftView.builder().id(wedId).staffId(staffId)
                .weekday(3).start("09:30").end("18:00").build());
        assertEquals("09:30", shiftService.findById(wedId).getStart(), "更新自身应放行");
        // 更新换到别人的星期应拒绝
        try {
            shiftService.update(StaffShiftView.builder().id(wedId).staffId(otherId)
                    .weekday(3).start("09:00").end("18:00").build());
            throw new AssertionError("更新撞他人班次应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("已有班次"));
        }
    }

    @Test
    void updateAndDeleteGuards() {
        Long staffId = newStaff("沈知远");
        Long id = shiftService.save(view(staffId, 1, "09:00", "18:00")).getId();
        shiftService.update(StaffShiftView.builder().id(id).staffId(staffId)
                .weekday(6).start("09:30").end("17:30").build());
        StaffShiftView saved = shiftService.findById(id);
        assertEquals(6, saved.getWeekday(), "更新应生效");
        assertEquals("周六", saved.getWeekdayText());
        assertEquals("09:30", saved.getStart());
        shiftService.deleteById(id);
        try {
            shiftService.findById(id);
            throw new AssertionError("删除后查询应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("班次不存在"));
        }
        try {
            shiftService.update(StaffShiftView.builder().id(99L).staffId(staffId)
                    .weekday(1).start("09:00").end("18:00").build());
            throw new AssertionError("未知 id 应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("班次不存在"));
        }
    }

    @Test
    void findPageFiltersDispatchOnNullCombos() {
        Long shenId = newStaff("沈知远");
        Long suId = newStaff("苏文若");
        shiftService.save(view(shenId, 1, "09:00", "18:00"));
        shiftService.save(view(shenId, 3, "09:00", "18:00"));
        shiftService.save(view(suId, 1, "09:30", "18:30"));

        assertEquals(3, shiftService.findPage(null, null, PageRequest.of(0, 10)).getTotalElements());
        assertEquals(2, shiftService.findPage(shenId, null, PageRequest.of(0, 10)).getTotalElements(), "按员工过滤");
        assertEquals(2, shiftService.findPage(null, 1, PageRequest.of(0, 10)).getTotalElements(), "按星期过滤");
        assertEquals(1, shiftService.findPage(suId, 1, PageRequest.of(0, 10)).getTotalElements(), "员工+星期联合过滤");
        assertEquals(0, shiftService.findPage(null, 7, PageRequest.of(0, 10)).getTotalElements());
        // 排序：员工、星期升序
        List<StaffShiftView> rows = shiftService.findPage(null, null, PageRequest.of(0, 10))
                .getContent().stream()
                .map(e -> StaffShiftView.FromEntity(e, null)).toList();
        assertTrue(rows.get(0).getWeekday() <= rows.get(1).getWeekday(), "同员工星期升序");
    }
}
