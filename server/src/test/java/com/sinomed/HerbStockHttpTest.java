package com.sinomed;

import com.sinomed.entity.HerbEntity;
import com.sinomed.repository.HerbRepository;
import com.sinomed.vo.HerbStockLogView;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 饮片出入库 HTTP 回归：入库 → 余额 → 出库 → 分页 → 删除，以及守卫与未登录 401。
 * 不开 @Transactional：RANDOM_PORT 下服务端事务看不到测试事务里的未提交种子数据。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/herb-stock-http-test-${random.uuid}.db"})
class HerbStockHttpTest {

    @LocalServerPort
    private int port;

    @Autowired
    private HerbRepository herbRepository;

    private final RestTemplate rest = new RestTemplate();

    private Long newHerb(String name) {
        HerbEntity herb = new HerbEntity();
        herb.setName(name);
        herb.setPrice(100);
        return herbRepository.save(herb).getId();
    }

    private String plusDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(String path, String token) {
        return rest.exchange(url(path), HttpMethod.GET, authEntity(null, token), Map.class).getBody();
    }

    /** /balance 直接返回列表，不走 PageResp 信封 */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getBalanceList(String path, String token) {
        return rest.exchange(url(path), HttpMethod.GET, authEntity(null, token), List.class).getBody();
    }

    @Test
    void inOutBalancePageAndDelete() {
        Long herbId = newHerb("甘草");
        String token = token();

        HerbStockLogView created = rest.postForEntity(url("/api/v1/herb-stock/"),
                authEntity(Map.of("herbId", herbId, "type", 1, "quantity", 1000,
                        "expiry", plusDays(90), "supplier", "亳州药市"), token), HerbStockLogView.class).getBody();
        assertEquals(herbId, created.getHerbId());
        assertEquals("甘草", created.getHerbName(), "回读联药材名");
        assertEquals(1, created.getType());

        rest.postForEntity(url("/api/v1/herb-stock/"),
                authEntity(Map.of("herbId", herbId, "type", 0, "quantity", 250), token), HerbStockLogView.class);

        List<Map<String, Object>> rows = getBalanceList("/api/v1/herb-stock/balance?expiryWithinDays=30", token);
        assertEquals(1, rows.size());
        assertEquals(750, ((Number) rows.get(0).get("stock")).intValue(), "入库 1000 - 出库 250");
        assertEquals(plusDays(90), rows.get(0).get("nextExpiry"));
        assertEquals(Boolean.FALSE, rows.get(0).get("warnExpiry"));

        Map<String, Object> page = getMap("/api/v1/herb-stock/page?current=1&pageSize=10&herbId=" + herbId, token);
        assertEquals(2, ((Number) page.get("total")).intValue());
        // id 倒序：第一条是出库流水
        Map<String, Object> first = (Map<String, Object>) ((List<?>) page.get("data")).get(0);
        assertEquals(0, ((Number) first.get("type")).intValue(), "最近一条是出库流水");

        Long outId = ((Number) first.get("id")).longValue();
        Map<String, Object> deleted = rest.exchange(url("/api/v1/herb-stock/" + outId),
                HttpMethod.DELETE, authEntity(null, token), Map.class).getBody();
        assertEquals("甘草", deleted.get("herbName"));
        assertEquals(1, ((Number) getMap("/api/v1/herb-stock/page?current=1&pageSize=10", token).get("total")).intValue());
    }

    @Test
    void guardsAndAuth() {
        Long herbId = newHerb("川芎");
        String token = token();
        // 非法效期
        try {
            rest.postForEntity(url("/api/v1/herb-stock/"),
                    authEntity(Map.of("herbId", herbId, "type", 1, "quantity", 10, "expiry", "2026/01/01"), token),
                    String.class);
            throw new AssertionError("非法效期应报 400");
        } catch (HttpStatusCodeException e) {
            assertEquals(400, e.getStatusCode().value());
            assertTrue(e.getResponseBodyAsString().contains("效期格式无效"), e.getResponseBodyAsString());
        }
        // 出库超库存
        try {
            rest.postForEntity(url("/api/v1/herb-stock/"),
                    authEntity(Map.of("herbId", herbId, "type", 0, "quantity", 999), token), String.class);
            throw new AssertionError("出库超库存应报 400");
        } catch (HttpStatusCodeException e) {
            assertEquals(400, e.getStatusCode().value());
            assertTrue(e.getResponseBodyAsString().contains("出库数量超过当前库存"), e.getResponseBodyAsString());
        }
        // 删除不存在的流水
        try {
            rest.exchange(url("/api/v1/herb-stock/9999"), HttpMethod.DELETE, authEntity(null, token), String.class);
            throw new AssertionError("删除不存在的流水应报 400");
        } catch (HttpStatusCodeException e) {
            assertEquals(400, e.getStatusCode().value());
            assertTrue(e.getResponseBodyAsString().contains("流水不存在"), e.getResponseBodyAsString());
        }
        // 未登录
        try {
            rest.getForEntity(url("/api/v1/herb-stock/balance"), String.class);
            throw new AssertionError("未登录应报 401");
        } catch (HttpStatusCodeException e) {
            assertEquals(401, e.getStatusCode().value());
        }
    }
}
