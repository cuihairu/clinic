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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 过敏审方 HTTP 回归：建档 → 记过敏史 → POST /allergy-check 命中，以及
 * 未知顾客 400 / 空白名单 400 / 未登录 401。走真实 HTTP 覆盖请求体绑定层。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/allergy-http-test-${random.uuid}.db"})
class PrescriptionAllergyHttpTest {

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

    private HttpEntity<?> authEntity(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    /** 预期 4xx 的请求：返回服务端错误体原文（RestTemplate 对 4xx 抛异常，这里解包） */
    private String exchange4xx(String path, Object body, String token) {
        try {
            return rest.exchange(url(path), HttpMethod.POST, authEntity(body, token), String.class).getBody();
        } catch (HttpStatusCodeException e) {
            return e.getResponseBodyAsString();
        }
    }

    @Test
    void allergyCheckFindsHerbsMentionedInHistory() {
        String token = token();

        Map<?, ?> customer = rest.exchange(url("/api/v1/customer/"), HttpMethod.POST,
                authEntity(Map.of("name", "过敏HTTP顾客", "gender", 0, "phone", "13900009201"), token), Map.class)
                .getBody();
        assertTrue(customer != null && customer.get("id") != null, "建档应返回 id：" + customer);
        Object customerId = customer.get("id");

        rest.exchange(url("/api/v1/customer/" + customerId + "/history"), HttpMethod.POST,
                authEntity(Map.of("type", 0, "content", "阿胶、蜂蜜过敏"), token), Map.class).getBody();

        Map<?, ?> result = rest.exchange(url("/api/v1/prescription/allergy-check"), HttpMethod.POST,
                authEntity(Map.of("customerId", customerId, "herbs", List.of("熟地黄", "阿胶", "砂仁")), token),
                Map.class).getBody();
        assertEquals(3, ((Number) result.get("checked")).intValue(), "比对数应为 3：" + result);
        List<?> findings = (List<?>) result.get("findings");
        assertEquals(1, findings.size(), "只应命中阿胶：" + findings);
        Map<?, ?> hit = (Map<?, ?>) findings.get(0);
        assertEquals("阿胶", hit.get("herb"));
        assertEquals("阿胶、蜂蜜过敏", hit.get("content"));
    }

    @Test
    void allergyCheckGuards() {
        String token = token();

        String noCust = exchange4xx("/api/v1/prescription/allergy-check",
                Map.of("customerId", 99999, "herbs", List.of("阿胶")), token);
        assertTrue(noCust != null && noCust.contains("顾客不存在"), "未知顾客应 400：" + noCust);

        Map<?, ?> customer = rest.exchange(url("/api/v1/customer/"), HttpMethod.POST,
                authEntity(Map.of("name", "过敏守卫顾客", "gender", 1, "phone", "13900009202"), token), Map.class)
                .getBody();
        Object customerId = customer.get("id");

        String blank = exchange4xx("/api/v1/prescription/allergy-check",
                Map.of("customerId", customerId, "herbs", List.of("  ")), token);
        assertTrue(blank != null && blank.contains("药材名单不能为空"), "空白名单应 400：" + blank);

        // 未登录 401（RestTemplate 对 4xx 抛异常）
        boolean threw = false;
        try {
            rest.exchange(url("/api/v1/prescription/allergy-check"), HttpMethod.POST, HttpEntity.EMPTY, Map.class);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw, "未登录应 401");
    }
}
