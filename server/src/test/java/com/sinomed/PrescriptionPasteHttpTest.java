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
 * 膏方领取流转 HTTP 回归：开膏方（落「待制作」）→ 待制作→可取→已取；
 * 非膏方拒绝、跳跃拒绝、未登录 401。走真实 HTTP（@RequestParam status 的绑定只有真请求才覆盖得到）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/paste-http-test-${random.uuid}.db"})
class PrescriptionPasteHttpTest {

    @LocalServerPort
    private int port;

    private final RestTemplate rest = new RestTemplate();

    private String token() {
        // 默认馆长账号（admin/123，UserDetailsServiceImpl 自举）直接登录
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

    private String exchange4xx(String method, String path, Object body, String token) {
        try {
            return rest.exchange(url(path), HttpMethod.valueOf(method), authEntity(body, token), String.class).getBody();
        } catch (HttpStatusCodeException e) {
            return e.getResponseBodyAsString();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String path, Object body, String token) {
        return rest.exchange(url(path), HttpMethod.POST, authEntity(body, token), Map.class).getBody();
    }

    private Map<String, Object> createCustomer(String token, String name, String phone) {
        return post("/api/v1/customer/", Map.of("name", name, "gender", 1, "phone", phone), token);
    }

    private Map<String, Object> createPrescription(String token, Object customerId, int type, String craft) {
        return post("/api/v1/prescription/",
                Map.of("customerId", customerId, "prescriptionType", type, "craft", craft, "doses", 30,
                        "herbs", List.of(Map.of("herb", "熟地黄", "weight", 60))), token);
    }

    @Test
    void pasteFlowOverHttp() {
        String token = token();
        Map<String, Object> customer = createCustomer(token, "膏方顾客", "13900009003");
        assertTrue(customer.get("id") != null, "建档应返回 id：" + customer);

        // 开膏方：落「待制作」、记收膏方式
        Map<String, Object> created = createPrescription(token, customer.get("id"), 1, "炼蜜");
        assertTrue(created.get("id") != null, "开方应返回 id：" + created);
        int id = (Integer) created.get("id");
        assertEquals(1, created.get("pasteStatus"));
        assertEquals("炼蜜", created.get("craft"));

        Map<String, Object> ready = rest.exchange(url("/api/v1/prescription/" + id + "/paste?status=2"),
                HttpMethod.PUT, authEntity(null, token), Map.class).getBody();
        assertEquals(2, ready.get("pasteStatus"));
        Map<String, Object> taken = rest.exchange(url("/api/v1/prescription/" + id + "/paste?status=3"),
                HttpMethod.PUT, authEntity(null, token), Map.class).getBody();
        assertEquals(3, taken.get("pasteStatus"));

        // 非膏方走膏方流转被拦（craft 非膏方不入库，传值被忽略）
        Map<String, Object> soup = createPrescription(token, customer.get("id"), 0, "炼蜜");
        String notPaste = exchange4xx("PUT", "/api/v1/prescription/" + soup.get("id") + "/paste?status=2", null, token);
        assertTrue(notPaste.contains("不是膏方"), "非膏方应 400：" + notPaste);

        // 待制作直接跳到已取被拦
        Map<String, Object> pending = createPrescription(token, customer.get("id"), 1, "清膏");
        String skip = exchange4xx("PUT", "/api/v1/prescription/" + pending.get("id") + "/paste?status=3", null, token);
        assertTrue(skip.contains("流转无效"), "跳跃应 400：" + skip);

        // 未登录 401（RestTemplate 对 4xx 抛异常）
        boolean threw = false;
        try {
            rest.exchange(url("/api/v1/prescription/" + id + "/paste?status=2"),
                    HttpMethod.PUT, HttpEntity.EMPTY, Map.class);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw, "未登录应 401");
    }
}
