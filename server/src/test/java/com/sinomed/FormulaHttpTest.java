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
 * 方剂库 HTTP 回归：建方剂 → 拼音/方名检索 → 更新 → 删除，以及守卫与未登录 401。
 * 走真实 HTTP 覆盖请求体绑定层。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/formula-http-test-${random.uuid}.db"})
class FormulaHttpTest {

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

    private String exchange4xx(String path, HttpMethod method, Object body, String token) {
        try {
            return rest.exchange(url(path), method, authEntity(body, token), String.class).getBody();
        } catch (HttpStatusCodeException e) {
            return e.getResponseBodyAsString();
        }
    }

    @Test
    void formulaCrudAndSearch() {
        String token = token();

        Map<?, ?> created = rest.exchange(url("/api/v1/formula/"), HttpMethod.POST,
                authEntity(Map.of("name", "当归四逆汤", "pinyin", "DangGuiSiNiTang", "source", "伤寒论",
                        "herbs", List.of(Map.of("herb", "当归", "weight", 9),
                                Map.of("herb", "桂枝", "weight", 9, "special", "后下"),
                                Map.of("herb", "白芍", "weight", 9))), token), Map.class).getBody();
        assertTrue(created != null && created.get("id") != null, "建档应返回 id：" + created);
        Long id = ((Number) created.get("id")).longValue();
        assertEquals(3, ((List<?>) created.get("herbs")).size());

        // 拼音检索：转小写后 contains 命中
        Map<?, ?> byPinyin = rest.exchange(url("/api/v1/formula/page?current=1&pageSize=10&keyword=sini"),
                HttpMethod.GET, authEntity(null, token), Map.class).getBody();
        assertEquals(1, ((Number) byPinyin.get("total")).intValue(), "拼音码应命中：" + byPinyin);
        // 方名检索
        Map<?, ?> byName = rest.exchange(url("/api/v1/formula/page?current=1&pageSize=10&keyword=四逆"),
                HttpMethod.GET, authEntity(null, token), Map.class).getBody();
        assertEquals(1, ((Number) byName.get("total")).intValue(), "方名应命中：" + byName);

        Map<?, ?> updated = rest.exchange(url("/api/v1/formula/"), HttpMethod.PUT,
                authEntity(Map.of("id", id, "name", "当归四逆汤", "pinyin", "dangguisinitang",
                        "herbs", List.of(Map.of("herb", "当归", "weight", 9))), token), Map.class).getBody();
        assertEquals(1, ((List<?>) updated.get("herbs")).size(), "更新应全量替换药味");

        rest.exchange(url("/api/v1/formula/" + id), HttpMethod.DELETE, authEntity(null, token), Map.class);
        String gone = exchange4xx("/api/v1/formula/" + id, HttpMethod.GET, null, token);
        assertTrue(gone != null && gone.contains("方剂不存在"), "删除后应 400：" + gone);
    }

    @Test
    void formulaGuards() {
        String token = token();

        String badPinyin = exchange4xx("/api/v1/formula/", HttpMethod.POST,
                Map.of("name", "新方", "pinyin", "Xin Fang1", "herbs", List.of(Map.of("herb", "甘草", "weight", 6))), token);
        assertTrue(badPinyin != null && badPinyin.contains("拼音检索码"), "非法拼音码应 400：" + badPinyin);

        String noHerb = exchange4xx("/api/v1/formula/", HttpMethod.POST,
                Map.of("name", "无药方", "pinyin", "wuyaofang", "herbs", List.of()), token);
        assertTrue(noHerb != null && noHerb.contains("方剂至少要有 1 味药"), "空药味应 400：" + noHerb);

        boolean threw = false;
        try {
            rest.exchange(url("/api/v1/formula/page?current=1&pageSize=10"), HttpMethod.GET,
                    HttpEntity.EMPTY, Map.class);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw, "未登录应 401");
    }
}
