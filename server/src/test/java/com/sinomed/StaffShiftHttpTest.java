package com.sinomed;

import com.sinomed.entity.StaffEntity;
import com.sinomed.repository.StaffRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 员工班表 HTTP 回归：加排班 → 分页过滤 → 整周列表 → 更新 → 删除，以及守卫与未登录 401。
 * 走真实 HTTP 覆盖请求体绑定层。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/staff-shift-http-test-${random.uuid}.db"})
class StaffShiftHttpTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StaffRepository staffRepository;

    private final RestTemplate rest = new RestTemplate();

    private Long newStaff(String name) {
        StaffEntity staff = new StaffEntity();
        staff.setName(name);
        staff.setAccount("shift-http-" + System.nanoTime());
        staff.setPassword("x");
        staff.setStatus(1);
        return staffRepository.save(staff).getId();
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

    private HttpEntity<?> authEntity(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private String exchange4xx(String path, HttpMethod method, Object body, String token) {
        try {
            return rest.exchange(url(path), method, authEntity(body, token), String.class).getBody();
        } catch (HttpStatusCodeException e) {
            return e.getResponseBodyAsString();
        }
    }

    @Test
    void shiftCrudPageAndWeekList() {
        String token = token();
        Long shenId = newStaff("沈知远");
        Long suId = newStaff("苏文若");

        Map<?, ?> created = rest.exchange(url("/api/v1/shift/"), HttpMethod.POST,
                authEntity(Map.of("staffId", shenId, "weekday", 3, "start", "09:00", "end", "18:00"),
                        token), Map.class).getBody();
        assertTrue(created != null && created.get("id") != null, "加排班应返回 id：" + created);
        Long wedId = ((Number) created.get("id")).longValue();
        assertEquals("沈知远", created.get("staffName"), "应联出员工姓名");
        assertEquals("周三", created.get("weekdayText"), "星期应给中文文案");

        rest.exchange(url("/api/v1/shift/"), HttpMethod.POST,
                authEntity(Map.of("staffId", shenId, "weekday", 4, "start", "09:00", "end", "18:00"),
                        token), Map.class).getBody();
        rest.exchange(url("/api/v1/shift/"), HttpMethod.POST,
                authEntity(Map.of("staffId", suId, "weekday", 3, "start", "09:30", "end", "18:30"),
                        token), Map.class).getBody();

        // 分页：全量 / 按员工 / 按星期 / 联合过滤
        assertEquals(3, pageTotal("/api/v1/shift/page?current=1&pageSize=10", token));
        assertEquals(2, pageTotal("/api/v1/shift/page?current=1&pageSize=10&staffId=" + shenId, token));
        assertEquals(2, pageTotal("/api/v1/shift/page?current=1&pageSize=10&weekday=3", token));
        assertEquals(1, pageTotal("/api/v1/shift/page?current=1&pageSize=10&staffId=" + suId + "&weekday=3", token));

        // 整周列表：返回数组，weekday 升序
        List<?> rows = rest.exchange(url("/api/v1/shift/staff/" + shenId),
                HttpMethod.GET, authEntity(null, token), List.class).getBody();
        assertTrue(rows != null && rows.size() == 2, "整周列表应 2 条：" + rows);
        assertEquals(3, ((Number) ((Map<?, ?>) rows.get(0)).get("weekday")).intValue(), "weekday 升序");

        Map<?, ?> updated = rest.exchange(url("/api/v1/shift/"), HttpMethod.PUT,
                authEntity(Map.of("id", wedId, "staffId", shenId, "weekday", 3,
                        "start", "09:30", "end", "17:30"), token), Map.class).getBody();
        assertEquals("09:30", updated.get("start"), "更新应生效");

        // 重复加排班应 400
        String dup = exchange4xx("/api/v1/shift/", HttpMethod.POST,
                Map.of("staffId", shenId, "weekday", 3, "start", "10:00", "end", "19:00"), token);
        assertTrue(dup != null && dup.contains("已有班次"), "重复排班应 400：" + dup);

        rest.exchange(url("/api/v1/shift/" + wedId), HttpMethod.DELETE, authEntity(null, token), Map.class);
        String gone = exchange4xx("/api/v1/shift/" + wedId, HttpMethod.GET, null, token);
        assertTrue(gone != null && gone.contains("班次不存在"), "删除后应 400：" + gone);
    }

    private int pageTotal(String path, String token) {
        Map<?, ?> page = rest.exchange(url(path), HttpMethod.GET, authEntity(null, token), Map.class).getBody();
        return ((Number) page.get("total")).intValue();
    }

    @Test
    void shiftGuards() {
        String token = token();
        Long staffId = newStaff("顾景明");

        String badWeekday = exchange4xx("/api/v1/shift/", HttpMethod.POST,
                Map.of("staffId", staffId, "weekday", 8, "start", "09:00", "end", "18:00"), token);
        assertTrue(badWeekday != null && badWeekday.contains("星期无效"), "星期越界应 400：" + badWeekday);

        String badTime = exchange4xx("/api/v1/shift/", HttpMethod.POST,
                Map.of("staffId", staffId, "weekday", 1, "start", "9:00", "end", "18:00"), token);
        assertTrue(badTime != null && badTime.contains("开始时间格式无效"), "非法时段应 400：" + badTime);

        String reversed = exchange4xx("/api/v1/shift/", HttpMethod.POST,
                Map.of("staffId", staffId, "weekday", 1, "start", "18:00", "end", "09:00"), token);
        assertTrue(reversed != null && reversed.contains("结束时间须晚于开始时间"), "时段倒置应 400：" + reversed);

        String noStaff = exchange4xx("/api/v1/shift/", HttpMethod.POST,
                Map.of("staffId", 9999, "weekday", 1, "start", "09:00", "end", "18:00"), token);
        assertTrue(noStaff != null && noStaff.contains("员工不存在"), "未知员工应 400：" + noStaff);

        boolean threw = false;
        try {
            rest.exchange(url("/api/v1/shift/page?current=1&pageSize=10"), HttpMethod.GET,
                    HttpEntity.EMPTY, Map.class);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw, "未登录应 401");
    }
}
