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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 穴位字典 HTTP 回归：收录 → 拼音/穴名检索 → 更新 → 删除，以及守卫与未登录 401。
 * 走真实 HTTP 覆盖请求体绑定层。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/acupoint-http-test-${random.uuid}.db"})
class AcupointHttpTest {

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
    void acupointCrudAndSearch() {
        String token = token();

        Map<?, ?> created = rest.exchange(url("/api/v1/acupoint/"), HttpMethod.POST,
                authEntity(Map.of("name", "四神聪", "pinyin", "SiShenCong", "meridian", "经外奇穴",
                        "location", "百会前后左右各 1 寸", "indication", "宁神醒脑，主治失眠、头痛"), token), Map.class).getBody();
        assertTrue(created != null && created.get("id") != null, "收录应返回 id：" + created);
        Long id = ((Number) created.get("id")).longValue();
        assertEquals("sishencong", created.get("pinyin"), "拼音码应转小写存储");

        // 拼音检索：转小写后 contains 命中
        Map<?, ?> byPinyin = rest.exchange(url("/api/v1/acupoint/page?current=1&pageSize=10&keyword=shencong"),
                HttpMethod.GET, authEntity(null, token), Map.class).getBody();
        assertEquals(1, ((Number) byPinyin.get("total")).intValue(), "拼音码应命中：" + byPinyin);
        // 穴名检索
        Map<?, ?> byName = rest.exchange(url("/api/v1/acupoint/page?current=1&pageSize=10&keyword=神聪"),
                HttpMethod.GET, authEntity(null, token), Map.class).getBody();
        assertEquals(1, ((Number) byName.get("total")).intValue(), "穴名应命中：" + byName);

        Map<?, ?> updated = rest.exchange(url("/api/v1/acupoint/"), HttpMethod.PUT,
                authEntity(Map.of("id", id, "name", "四神聪", "pinyin", "sishencong",
                        "meridian", "经外奇穴", "indication", "主治失眠、健忘"), token), Map.class).getBody();
        assertEquals("主治失眠、健忘", updated.get("indication"), "更新应生效");

        rest.exchange(url("/api/v1/acupoint/" + id), HttpMethod.DELETE, authEntity(null, token), Map.class);
        String gone = exchange4xx("/api/v1/acupoint/" + id, HttpMethod.GET, null, token);
        assertTrue(gone != null && gone.contains("穴位不存在"), "删除后应 400：" + gone);
    }

    @Test
    void acupointGuards() {
        String token = token();

        String badPinyin = exchange4xx("/api/v1/acupoint/", HttpMethod.POST,
                Map.of("name", "新穴", "pinyin", "Xin Xue1", "meridian", "经外奇穴"), token);
        assertTrue(badPinyin != null && badPinyin.contains("拼音检索码"), "非法拼音码应 400：" + badPinyin);

        String noMeridian = exchange4xx("/api/v1/acupoint/", HttpMethod.POST,
                Map.of("name", "新穴", "pinyin", "xinxue"), token);
        assertTrue(noMeridian != null && noMeridian.contains("归经不能为空"), "缺归经应 400：" + noMeridian);

        boolean threw = false;
        try {
            rest.exchange(url("/api/v1/acupoint/page?current=1&pageSize=10"), HttpMethod.GET,
                    HttpEntity.EMPTY, Map.class);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw, "未登录应 401");
    }
}
