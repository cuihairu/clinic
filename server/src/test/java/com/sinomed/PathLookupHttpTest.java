package com.sinomed;

import com.sinomed.entity.ItemEntity;
import com.sinomed.entity.StaffEntity;
import com.sinomed.repository.ItemRepository;
import com.sinomed.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 路径变量查询回归：staff/phone、staff/name、item/name 曾缺 @PathVariable，
 * 参数恒为 null → QBE 探针全空 → 全表查询（手机号报 NonUnique、按名查静默拿错行）。
 * 必须走 HTTP 断言（服务层直调测不出该缺陷）。
 * 库路径随机文件避免污染。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/path-lookup-test-${random.uuid}.db"
})
class PathLookupHttpTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate = new RestTemplate();

    private String token;

    @BeforeEach
    void seed() {
        if (staffRepository.count() >= 3) {
            token = login();
            return;
        }
        newStaff("顾景明", "lookup-gu", "13800000001");
        newStaff("沈知远", "lookup-shen", "13800000002");
        newStaff("苏文若", "lookup-su", "13800000003");
        ItemEntity item = new ItemEntity();
        item.setName("lookup-推拿");
        item.setPrice(680);
        item.setDescription("desc");
        item.setEnabled(1);
        item.setSort(1);
        itemRepository.save(item);
        ItemEntity other = new ItemEntity();
        other.setName("lookup-艾灸");
        other.setPrice(880);
        other.setDescription("desc");
        other.setEnabled(1);
        other.setSort(2);
        itemRepository.save(other);
        token = login();
    }

    private void newStaff(String name, String account, String phone) {
        StaffEntity s = new StaffEntity();
        s.setName(name);
        s.setAccount(account);
        s.setPhone(phone);
        s.setRole(99);
        s.setStatus(1);
        s.setPassword(passwordEncoder.encode("123"));
        staffRepository.save(s);
    }

    private String login() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var resp = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/user/login",
                new HttpEntity<>(Map.of("username", "lookup-gu", "password", "123"), headers), Map.class);
        assertEquals(200, resp.getStatusCode().value(), "login failed: " + resp.getBody());
        return (String) resp.getBody().get("token");
    }

    private String get(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        try {
            var resp = restTemplate.exchange("http://localhost:" + port + url,
                    HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return resp.getBody();
        } catch (Exception e) {
            return "EXC: " + e.getMessage();
        }
    }

    @Test
    void staffPhoneLookupFiltersByPhone() {
        String body = get("/api/v1/staff/phone/13800000002");
        assertTrue(body.contains("沈知远"), "按手机号应命中沈知远: " + body);
        assertTrue(!body.contains("顾景明"), "不应串到其他员工: " + body);
    }

    @Test
    void staffPhoneLookupUnknownPhoneReportsMissingNotTableScan() {
        String body = get("/api/v1/staff/phone/13999999999");
        // 修复后：未知手机号应报「员工不存在」（探针生效查无此行），而非全表扫描的 500
        assertTrue(body.contains("员工不存在"), "未知手机号应报员工不存在: " + body);
        assertTrue(!body.contains("NonUnique"), "不应再出现全表扫描冲突: " + body);
    }

    @Test
    void staffNameLookupFiltersByName() {
        String body = get("/api/v1/staff/name/苏文若");
        assertTrue(body.contains("苏文若") && body.contains("13800000003"), "按姓名应命中苏文若: " + body);
        assertTrue(!body.contains("沈知远"), "不应串到其他员工: " + body);
    }

    @Test
    void itemLookupByNameFiltersByName() {
        String body = get("/api/v1/item/name/lookup-艾灸");
        assertTrue(body.contains("880"), "按名应命中艾灸(880): " + body);
        assertTrue(!body.contains("680"), "不应串到推拿(680): " + body);
    }
}
