package com.sinomed;

import com.sinomed.entity.StaffEntity;
import com.sinomed.entity.StaffLeaveEntity;
import com.sinomed.repository.StaffLeaveRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.StaffLeaveService;
import com.sinomed.vo.StaffLeaveView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 员工请假（StaffLeaveService）回归：提交校验（员工/类型/事由/起止时间）、
 * 分页与按员工过滤、删除。库路径随机文件避免污染。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/staff-leave-test-${random.uuid}.db"
})
class StaffLeaveTest {

    @Autowired
    private StaffLeaveService staffLeaveService;

    @Autowired
    private StaffLeaveRepository staffLeaveRepository;

    @Autowired
    private StaffRepository staffRepository;

    /** 每个用例独立员工，避免方法执行顺序耦合 */
    private Long newStaff(String name) {
        StaffEntity staff = new StaffEntity();
        staff.setName(name);
        staff.setAccount("leave-" + System.nanoTime());
        staff.setPassword("x");
        staff.setStatus(1);
        return staffRepository.save(staff).getId();
    }

    private StaffLeaveView view(Long staffId, Integer leaveType, String reason, Date start, Date end) {
        return StaffLeaveView.builder()
                .staffId(staffId).leaveType(leaveType).reason(reason)
                .startTime(start).endTime(end).build();
    }

    @Test
    void createSavesLeaveWithValidations() {
        Long staffA = newStaff("请假医师甲");
        Date now = new Date();
        Date later = new Date(now.getTime() + 86_400_000L);
        StaffLeaveEntity leave = staffLeaveService.create(view(staffA, 1, " 家中急事 ", now, later));
        assertTrue(leave.getId() > 0);
        assertEquals(1, leave.getLeaveType());
        assertEquals("家中急事", leave.getReason(), "事由应去首尾空白");

        // 全部校验分支
        var e0 = assertThrows(IllegalArgumentException.class, () -> staffLeaveService.create(view(null, 0, "事由", now, later)));
        assertEquals("员工id不能为空", e0.getMessage());
        var e1 = assertThrows(IllegalArgumentException.class, () -> staffLeaveService.create(view(99999L, 0, "事由", now, later)));
        assertEquals("员工不存在：99999", e1.getMessage());
        var e2 = assertThrows(IllegalArgumentException.class, () -> staffLeaveService.create(view(staffA, 2, "事由", now, later)));
        assertEquals("类型无效：0 病假 / 1 事假", e2.getMessage());
        var e3 = assertThrows(IllegalArgumentException.class, () -> staffLeaveService.create(view(staffA, 0, " ", now, later)));
        assertEquals("事由不能为空", e3.getMessage());
        var e4 = assertThrows(IllegalArgumentException.class, () -> staffLeaveService.create(view(staffA, 0, "事由", null, later)));
        assertEquals("起止时间不能为空", e4.getMessage());
        var e5 = assertThrows(IllegalArgumentException.class, () -> staffLeaveService.create(view(staffA, 0, "事由", later, now)));
        assertEquals("结束时间不能早于起始时间", e5.getMessage());
    }

    @Test
    void findPageFiltersByStaffNewestFirst() {
        Long mine = newStaff("请假医师甲");
        Long other = newStaff("请假医师乙");
        Date now = new Date();
        staffLeaveService.create(view(mine, 0, "病假一天", now, now));
        staffLeaveService.create(view(other, 1, "事由乙", now, now));
        staffLeaveService.create(view(mine, 1, "事由甲二", now, now));

        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        var onlyMine = staffLeaveService.findPage(mine, pageable);
        assertEquals(2, onlyMine.getTotalElements());
        assertEquals("事由甲二", onlyMine.getContent().get(0).getReason(), "id 倒序 newest first");
        assertTrue(onlyMine.getContent().stream().allMatch(l -> l.getStaffId().equals(mine)));
        assertEquals(1, staffLeaveService.findPage(other, pageable).getTotalElements());
    }

    @Test
    void deleteRemovesAndRejectsUnknown() {
        Long staffB = newStaff("请假医师乙");
        Date now = new Date();
        StaffLeaveEntity leave = staffLeaveService.create(view(staffB, 0, "病假", now, now));
        staffLeaveService.deleteById(leave.getId());
        assertTrue(staffLeaveRepository.findById(leave.getId()).isEmpty());

        var e = assertThrows(IllegalArgumentException.class, () -> staffLeaveService.deleteById(99999L));
        assertEquals("请假记录不存在：99999", e.getMessage());
    }
}
