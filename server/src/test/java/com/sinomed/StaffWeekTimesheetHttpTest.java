package com.sinomed;

import com.sinomed.entity.SignEntity;
import com.sinomed.entity.StaffEntity;
import com.sinomed.repository.SignRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.StaffShiftService;
import com.sinomed.vo.StaffShiftView;
import com.sinomed.vo.StaffWeekTimesheetView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 周班次对照 HTTP 回归：结构读取（周一锚定、计划/打卡对照）、非法周日期 400、未登录 401。
 * 不开 @Transactional：RANDOM_PORT 下服务端事务看不到测试事务里的未提交种子数据。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/staff-week-timesheet-http-test-${random.uuid}.db"})
class StaffWeekTimesheetHttpTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StaffShiftService shiftService;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private SignRepository signRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RestTemplate rest = new RestTemplate();

    private Long newStaff(String name) {
        StaffEntity staff = new StaffEntity();
        staff.setName(name);
        staff.setAccount("week-ts-http-" + System.nanoTime());
        staff.setPassword("x");
        staff.setStatus(1);
        return staffRepository.save(staff).getId();
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

    /** type 1 上班 0 下班；落库后 SQL 回写绕开 @CreatedDate 覆盖 */
    private void sign(Long staffId, String day, int hour, int minute, int type) {
        SignEntity sign = new SignEntity();
        sign.setStaffId(staffId);
        sign.setType(type);
        Long id = signRepository.save(sign).getId();
        Timestamp time = new Timestamp(at(day, hour, minute).getTime());
        jdbcTemplate.update("UPDATE signs SET create_time = ?, update_time = ? WHERE id = ?", time, time, id);
    }

    private String token() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var resp = rest.postForEntity(url("/api/v1/user/login"),
                new HttpEntity<>(Map.of("username", "admin", "password", "123"), headers), Map.class);
        return (String) resp.getBody().get("token");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void weekTimesheetStructureViaHttp() {
        Long staffId = newStaff("甲");
        shiftService.save(StaffShiftView.builder().staffId(staffId).weekday(1).start("09:00").end("18:00").build());
        sign(staffId, "2026-10-05", 9, 5, 1);
        sign(staffId, "2026-10-05", 17, 55, 0);

        StaffWeekTimesheetView view = rest.exchange(url("/api/v1/staff/timesheet/week?weekDate=2026-10-07"),
                HttpMethod.GET, new HttpEntity<>(auth(token())),
                StaffWeekTimesheetView.class).getBody();

        assertEquals("2026-10-05", view.getWeekStart());
        // 首次 admin 登录会自动建 admin 员工行，按 id 找本测试的员工
        var row = view.getStaffs().stream()
                .filter(r -> staffId.equals(r.getStaffId())).findFirst().orElseThrow();
        var days = row.getDays();
        assertEquals(7, days.size());
        assertEquals("甲", row.getName());
        assertEquals("周一", days.get(0).getWeekdayText());
        assertEquals("09:00", days.get(0).getPlanStart());
        assertEquals("09:05", days.get(0).getSignStart());
        assertEquals("17:55", days.get(0).getSignEnd());
        assertEquals(8L, days.get(0).getHours());
    }

    @Test
    void guardsViaHttp() {
        String token = token();
        try {
            rest.exchange(url("/api/v1/staff/timesheet/week?weekDate=abc"),
                    HttpMethod.GET, new HttpEntity<>(auth(token)), String.class);
            throw new AssertionError("非法周日期应报 400");
        } catch (HttpStatusCodeException e) {
            assertEquals(400, e.getStatusCode().value());
            assertTrue(e.getResponseBodyAsString().contains("无效的周日期"), e.getResponseBodyAsString());
        }
        try {
            rest.exchange(url("/api/v1/staff/timesheet/week?weekDate=2026-10-07"),
                    HttpMethod.GET, HttpEntity.EMPTY, String.class);
            throw new AssertionError("未登录应报 401");
        } catch (HttpStatusCodeException e) {
            assertEquals(401, e.getStatusCode().value());
        }
    }
}
