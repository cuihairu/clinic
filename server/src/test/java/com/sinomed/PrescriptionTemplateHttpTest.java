package com.sinomed;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 病症处方模板 HTTP 回归：建/改/查/删 + 名称唯一 + 药味全量替换 + 上架过滤。
 * 走真实 HTTP（controller 参数绑定与 JSON 序列化层只有真请求才覆盖得到）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.datasource.url=jdbc:sqlite:target/template-http-test-${random.uuid}.db"})
class PrescriptionTemplateHttpTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Test
    void templateCrudAndItemReplacement() {
        String token = token();

        // 建：2 味药 + 建议剂数/用法
        Map<String, Object> body = Map.of(
                "name", "风寒感冒",
                "doses", 5,
                "decoction", 1,
                "usage", "水煎服，日一剂",
                "herbs", List.of(Map.of("herb", "荆芥", "weight", 10), Map.of("herb", "防风", "weight", 10)));
        Map<?, ?> created = rest.exchange(url("/api/v1/prescription/template/"), HttpMethod.POST,
                authEntity(body, token), Map.class).getBody();
        assertTrue(created != null && created.get("id") != null, "创建应返回 id：" + created);
        int id = (Integer) created.get("id");
        assertEquals(5, created.get("doses"));
        assertEquals(2, ((List<?>) created.get("herbs")).size());

        // 重名拒绝
        String dup = exchange4xx("POST", "/api/v1/prescription/template/", body, token);
        assertTrue(dup != null && dup.contains("模板名已存在"), "重名应报 400：" + dup);

        // 改：药味全量替换（2 → 3 味）+ 换名
        Map<String, Object> upd = Map.of(
                "id", id,
                "name", "风寒感冒（轻症）",
                "doses", 7,
                "herbs", List.of(
                        Map.of("herb", "荆芥", "weight", 10),
                        Map.of("herb", "防风", "weight", 10),
                        Map.of("herb", "紫苏叶", "weight", 9)));
        Map<?, ?> updated = rest.exchange(url("/api/v1/prescription/template/"), HttpMethod.PUT,
                authEntity(upd, token), Map.class).getBody();
        assertTrue(updated != null && updated.get("id") != null, "更新应成功：" + updated);
        assertEquals("风寒感冒（轻症）", updated.get("name"));
        assertEquals(3, ((List<?>) updated.get("herbs")).size(), "药味应全量替换为 3 味：" + updated);
        assertEquals("紫苏叶", ((List<?>) updated.get("herbs")).stream()
                .map(h -> (Map<?, ?>) h).filter(h -> "紫苏叶".equals(h.get("herb")))
                .map(h -> h.get("herb")).findFirst().orElse(null));

        // 详情与列表看到的是替换后的数据
        Map<String, Object> detail = get("/api/v1/prescription/template/" + id, token);
        assertEquals(3, ((List<?>) detail.get("herbs")).size());

        // 改名撞其他模板才拒：自己同名（仅调剂数）应放行
        Map<String, Object> selfRename = Map.of(
                "id", id,
                "name", "风寒感冒（轻症）",
                "doses", 9,
                "herbs", List.of(Map.of("herb", "荆芥", "weight", 10)));
        Map<?, ?> self = rest.exchange(url("/api/v1/prescription/template/"), HttpMethod.PUT,
                authEntity(selfRename, token), Map.class).getBody();
        assertTrue(self != null && self.get("id") != null, "同 id 同名应放行：" + self);
        assertEquals(9, self.get("doses"));

        // 停用后不在上架列表；重新启用后回来
        Map<String, Object> disable = Map.of(
                "id", id, "name", "风寒感冒（轻症）", "doses", 9, "enabled", 0,
                "herbs", List.of(Map.of("herb", "荆芥", "weight", 10)));
        rest.exchange(url("/api/v1/prescription/template/"), HttpMethod.PUT, authEntity(disable, token), Map.class);
        List<?> disabledList = (List<?>) get("/api/v1/prescription/template/enabled", token).get("data");
        assertTrue(disabledList == null || disabledList.stream().noneMatch(t -> ((Map<?, ?>) t).get("id").equals(id)),
                "停用模板不应出现在上架列表");

        // 分页含停用模板（管理端口径）
        Map<String, Object> page = get("/api/v1/prescription/template/page?current=1&pageSize=10", token);
        assertTrue(((List<?>) page.get("data")).stream().anyMatch(t -> ((Map<?, ?>) t).get("id").equals(id)),
                "分页应含停用模板：" + page);

        // 删除后再查 400
        rest.exchange(url("/api/v1/prescription/template/" + id), HttpMethod.DELETE, authEntity(null, token), Map.class);
        Map<String, Object> gone = get("/api/v1/prescription/template/" + id, token);
        assertTrue(gone != null && String.valueOf(gone).contains("模板不存在"), "删除后详情应 400：" + gone);
    }

    @Test
    void templateValidationGuards() {
        String token = token();

        // 无药味拒绝
        String noHerbs = exchange4xx("POST", "/api/v1/prescription/template/",
                Map.of("name", "无药模板", "herbs", List.of()), token);
        assertTrue(noHerbs != null && noHerbs.contains("至少要有 1 味药"), "无药味应报 400：" + noHerbs);

        // 剂量 <= 0 拒绝
        String badWeight = exchange4xx("POST", "/api/v1/prescription/template/",
                Map.of("name", "坏剂量模板", "herbs", List.of(Map.of("herb", "甘草", "weight", 0))), token);
        assertTrue(badWeight != null && badWeight.contains("剂量必须大于 0"), "零剂量应报 400：" + badWeight);

        // 名字超 20 字拒绝
        String longName = exchange4xx("POST", "/api/v1/prescription/template/",
                Map.of("name", "一二三四五六七八九十一二三四五六七八九十一", "herbs", List.of(Map.of("herb", "甘草", "weight", 6))), token);
        assertTrue(longName != null && longName.contains("20 字"), "超长名应报 400：" + longName);

        // 未登录 401（RestTemplate 对 4xx 抛异常）
        boolean threw = false;
        try {
            rest.exchange(url("/api/v1/prescription/template/page?current=1&pageSize=5"),
                    HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw, "未登录应 401");
    }
}
