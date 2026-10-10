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
 * 次卡核销流水 HTTP 回归：发卡 → 建单 → 次卡结算（扣 1 次实收 0）→ 核销记录
 * （第几次/订单/时间顺序），以及无卡结算拒绝与未登录 401。
 * 走真实 HTTP（路径变量与参数绑定层只有真请求才覆盖得到）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/card-usage-http-test-${random.uuid}.db"})
class CardUsageHttpTest {

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

    private HttpEntity<?> jsonEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<?> authEntity(Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> get(String path, String token) {
        try {
            return rest.exchange(url(path), HttpMethod.GET, authEntity(null, token), Map.class).getBody();
        } catch (Exception e) {
            return Map.of("EXC", String.valueOf(e.getMessage()));
        }
    }

    /** 预期 4xx 的请求：返回服务端错误体原文（RestTemplate 对 4xx 抛异常，这里解包） */
    private String exchange4xx(String method, String path, Object body, String token) {
        try {
            return rest.exchange(url(path), HttpMethod.valueOf(method), authEntity(body, token), String.class).getBody();
        } catch (HttpStatusCodeException e) {
            return e.getResponseBodyAsString();
        }
    }

    private Map<String, Object> issueCard(String token, Object customerId, Object itemId, int totalTimes) {
        return rest.exchange(url("/api/v1/card/"), HttpMethod.POST,
                authEntity(Map.of("customerId", customerId, "itemId", itemId, "totalTimes", totalTimes), token),
                Map.class).getBody();
    }

    private Map<String, Object> createOrder(String token, Object customerId, Object itemId) {
        return rest.exchange(url("/api/v1/order/"), HttpMethod.POST,
                authEntity(Map.of("customerId", customerId, "itemId", itemId), token), Map.class).getBody();
    }

    @Test
    void usageFlowIssueOrderSettleQuery() {
        String token = token();

        Map<?, ?> customer = rest.exchange(url("/api/v1/customer/"), HttpMethod.POST,
                authEntity(Map.of("name", "疗程顾客", "gender", 0, "phone", "13900009001"), token), Map.class)
                .getBody();
        assertTrue(customer != null && customer.get("id") != null, "建档应返回 id：" + customer);
        Map<?, ?> item = rest.exchange(url("/api/v1/item/"), HttpMethod.POST,
                authEntity(Map.of("name", "核销测试推拿卡", "price", 680, "description", ""), token), Map.class)
                .getBody();
        assertTrue(item != null && item.get("id") != null, "建卡项应返回 id：" + item);

        // 发 10 次卡，先后两单都走次卡结算 → 两条核销，第 1/2 次、余 8
        Map<?, ?> card = issueCard(token, customer.get("id"), item.get("id"), 10);
        assertTrue(card != null && card.get("id") != null, "发卡应返回 id：" + card);
        int cardId = (Integer) card.get("id");

        Map<?, ?> order1 = createOrder(token, customer.get("id"), item.get("id"));
        assertTrue(order1 != null && order1.get("id") != null, "建单应返回 id：" + order1);
        Map<?, ?> settle1 = rest.exchange(url("/api/v1/settlement/"), HttpMethod.POST,
                authEntity(Map.of("orderId", order1.get("id"), "payType", 5), token), Map.class).getBody();
        assertTrue(settle1 != null && Integer.valueOf(0).equals(settle1.get("money")),
                "次卡结算应实收 0：" + settle1);

        List<?> usages = rest.exchange(url("/api/v1/card/" + cardId + "/usages"),
                HttpMethod.GET, authEntity(null, token), List.class).getBody();
        assertTrue(usages != null && usages.size() == 1, "一次结算应落一条核销：" + usages);
        Map<?, ?> u1 = (Map<?, ?>) usages.get(0);
        assertEquals(1, u1.get("timesUsed"), "首次核销应是第 1 次：" + u1);
        assertEquals(order1.get("id"), u1.get("orderId"), "核销应关联订单：" + u1);
        assertTrue(u1.get("createTime") != null, "核销应带时间：" + u1);

        Map<?, ?> order2 = createOrder(token, customer.get("id"), item.get("id"));
        rest.exchange(url("/api/v1/settlement/"), HttpMethod.POST,
                authEntity(Map.of("orderId", order2.get("id"), "payType", 5), token), Map.class).getBody();

        var cardsResp = rest.exchange(url("/api/v1/card/list?customerId=" + customer.get("id")),
                HttpMethod.GET, authEntity(null, token), List.class).getBody();
        assertTrue(cardsResp != null && cardsResp.size() == 1, "应只有一张卡");
        assertEquals(8, ((Map<?, ?>) cardsResp.get(0)).get("remainingTimes"), "两次抵扣后应余 8 次");

        List<?> usages2 = rest.exchange(url("/api/v1/card/" + cardId + "/usages"),
                HttpMethod.GET, authEntity(null, token), List.class).getBody();
        assertTrue(usages2 != null && usages2.size() == 2, "两次结算应落两条核销：" + usages2);
        assertEquals(2, ((Map<?, ?>) usages2.get(0)).get("timesUsed"), "新记录在前，应先看到第 2 次：" + usages2);
        assertEquals(1, ((Map<?, ?>) usages2.get(1)).get("timesUsed"));
    }

    @Test
    void usageGuards() {
        String token = token();

        // 未发卡顾客：建单走次卡结算应 400，且不落任何核销
        Map<?, ?> customer = rest.exchange(url("/api/v1/customer/"), HttpMethod.POST,
                authEntity(Map.of("name", "无卡顾客", "gender", 1, "phone", "13900009002"), token), Map.class)
                .getBody();
        Map<?, ?> item = rest.exchange(url("/api/v1/item/"), HttpMethod.POST,
                authEntity(Map.of("name", "核销守卫卡项", "price", 100, "description", ""), token), Map.class)
                .getBody();
        Map<?, ?> order = createOrder(token, customer.get("id"), item.get("id"));
        String rejected = exchange4xx("POST", "/api/v1/settlement/",
                Map.of("orderId", order.get("id"), "payType", 5), token);
        assertTrue(rejected != null && rejected.contains("无可抵扣次卡"), "无卡结算应 400：" + rejected);

        // 不存在的持卡查核销：400 持卡不存在
        String gone = exchange4xx("GET", "/api/v1/card/99999/usages", null, token);
        assertTrue(gone != null && gone.contains("持卡不存在"), "未知持卡应 400：" + gone);

        // 未登录 401（RestTemplate 对 4xx 抛异常）
        boolean threw = false;
        try {
            rest.exchange(url("/api/v1/card/1/usages"), HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw, "未登录应 401");
    }
}
