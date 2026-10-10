package com.sinomed;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 月度收费报表 HTTP 回归：GET /api/v1/settlement/report/month 空月返回空聚合，
 * 非法月份 400，未登录 401。走真实 HTTP 覆盖参数绑定层。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/settle-report-http-test-${random.uuid}.db"})
class SettlementReportHttpTest {

    @LocalServerPort
    private int port;

    private final RestTemplate rest = new RestTemplate();

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

    private String exchange4xx(String path, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
        } catch (HttpStatusCodeException e) {
            return e.getResponseBodyAsString();
        }
    }

    @Test
    void monthReportReturnsEmptyAggregationForFreshMonth() {
        String token = token();
        String month = LocalDate.now().toString().substring(0, 7);

        Map<?, ?> report = rest.exchange(url("/api/v1/settlement/report/month?month=" + month),
                HttpMethod.GET, authGet(token), Map.class).getBody();
        assertEquals(month, report.get("month"));
        assertEquals(0, ((Number) report.get("totalCount")).intValue(), "空库应无结算单：" + report);
        assertTrue(((java.util.List<?>) report.get("days")).isEmpty());
        assertTrue(((java.util.List<?>) report.get("payTypes")).isEmpty());
    }

    @Test
    void monthReportGuards() {
        String token = token();

        String bad = exchange4xx("/api/v1/settlement/report/month?month=abc", token);
        assertTrue(bad != null && bad.contains("报表月份格式无效"), "非法月份应 400：" + bad);

        boolean threw = false;
        try {
            rest.exchange(url("/api/v1/settlement/report/month?month=" + LocalDate.now().toString().substring(0, 7)),
                    HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw, "未登录应 401");
    }

    private HttpEntity<Void> authGet(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}
