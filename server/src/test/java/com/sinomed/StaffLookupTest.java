package com.sinomed;

import com.sinomed.entity.StaffEntity;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.StaffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 员工查询（StaffService.findByName / findByPhone）回归：
 * 手机号接口曾误调按姓名匹配（api.md 标「待修」）——锁死两条查询互不串。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/staff-lookup-test-${random.uuid}.db"
})
class StaffLookupTest {

    @Autowired
    private StaffService staffService;

    @Autowired
    private StaffRepository staffRepository;

    @BeforeEach
    void seed() {
        // 随机空库无种子，幂等建档
        if (staffService.findByPhone("13900009001").isEmpty()) {
            StaffEntity staff = new StaffEntity();
            staff.setName("张三丰");
            staff.setAccount("lookup-" + System.nanoTime());
            staff.setPassword("x");
            staff.setPhone("13900009001");
            staff.setStatus(1);
            staffRepository.save(staff);
        }
    }

    @Test
    void findByPhoneMatchesPhoneNotName() {
        var byPhone = staffService.findByPhone("13900009001");
        assertTrue(byPhone.isPresent());
        assertEquals("张三丰", byPhone.get().getName());
    }

    @Test
    void findByNameMatchesNameNotPhone() {
        var byName = staffService.findByName("张三丰");
        assertTrue(byName.isPresent());
        // 姓名查询不应错拿手机号命中
        assertEquals("13900009001", byName.get().getPhone());
        assertTrue(staffService.findByName("13900009001").isEmpty(), "按姓名查手机号应无命中");
        assertTrue(staffService.findByPhone("张三丰").isEmpty(), "按手机号查姓名应无命中");
    }
}
