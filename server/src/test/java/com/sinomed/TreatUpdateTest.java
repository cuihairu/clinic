package com.sinomed;

import com.sinomed.entity.CustomerEntity;
import com.sinomed.entity.TreatEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.TreatRepository;
import com.sinomed.service.TreatService;
import com.sinomed.vo.TreatView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 接诊单更新（TreatService.update）回归：原 PUT 为只带 id 的空壳（会把整单字段清空）——
 * 锁死「id 必填且须存在、字段全量以请求体为准、更新后可查到新值」。
 * 库路径随机文件避免污染。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/treat-update-test-${random.uuid}.db"
})
class TreatUpdateTest {

    @Autowired
    private TreatService treatService;

    @Autowired
    private TreatRepository treatRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private static Long customerId;

    @BeforeEach
    void seed() {
        if (customerId == null) {
            CustomerEntity customer = new CustomerEntity();
            customer.setName("接诊顾客");
            customer.setPhone("13900009501");
            customerId = customerRepository.save(customer).getId();
        }
    }

    private TreatView view(Long id, String desc, String plan) {
        TreatView v = new TreatView();
        v.setId(id);
        v.setCustomerId(customerId);
        v.setDesc(desc);
        v.setPlan(plan);
        return v;
    }

    @Test
    void updateOverwritesFieldsAndStaysReadable() {
        Long id = treatService.save(view(null, "初诊主诉", "初诊方案").ToTreatEntity()).getId();
        assertTrue(id > 0);

        TreatEntity updated = treatService.update(view(id, "复诊主诉", "调整方案"));
        assertEquals(id, updated.getId());
        assertEquals("复诊主诉", updated.getDesc());
        assertEquals("调整方案", updated.getPlan());

        // 更新后重查（绕过缓存新开持久化上下文经仓内 @Cacheable 语义）应见新值
        assertEquals("复诊主诉", treatRepository.findById(id).orElseThrow().getDesc());
    }

    @Test
    void updateRejectsMissingUnknownAndKeepsOriginal() {
        int before = treatRepository.findAll().size();
        var e0 = assertThrows(IllegalArgumentException.class, () -> treatService.update(view(null, "x", "y")));
        assertEquals("接诊单id不能为空", e0.getMessage());

        var e1 = assertThrows(IllegalArgumentException.class, () -> treatService.update(view(99999L, "x", "y")));
        assertEquals("接诊单不存在：99999", e1.getMessage());
        assertEquals(before, treatRepository.findAll().size(), "失败更新不应落单");
    }

    /** 针灸处方：针法/留针/手法/疗程随接诊单保存、更新、回读；空白文本按空处理 */
    @Test
    void acupunctureFieldsRoundTrip() {
        TreatView v = view(null, "夜寐不安", "针灸调理");
        v.setAcuMethod("  毫针、耳穴压豆  ");
        v.setRetentionMinutes(25);
        v.setManipulation("平补平泻");
        v.setAcuCourse("每周 2 次 × 2 周");
        Long id = treatService.save(v.ToTreatEntity()).getId();

        TreatEntity saved = treatRepository.findById(id).orElseThrow();
        assertEquals("毫针、耳穴压豆", saved.getAcuMethod(), "存库前去首尾空白");
        assertEquals(25, saved.getRetentionMinutes());
        assertEquals("平补平泻", saved.getManipulation());
        assertEquals("每周 2 次 × 2 周", saved.getAcuCourse());

        TreatView updated = view(id, "复诊", "继续针灸");
        updated.setAcuMethod("温针");
        updated.setRetentionMinutes(30);
        updated.setManipulation("补法");
        updated.setAcuCourse("  ");
        treatService.update(updated);
        TreatEntity after = treatRepository.findById(id).orElseThrow();
        assertEquals("温针", after.getAcuMethod());
        assertEquals(30, after.getRetentionMinutes());
        assertEquals("补法", after.getManipulation());
        assertNull(after.getAcuCourse(), "空白疗程应按空处理");
    }

    /** 针灸处方守卫：针法/手法超 30 字、留针越界（0 / 241）、疗程超 100 字均 400 */
    @Test
    void acupunctureGuards() {
        record Case(String acuMethod, Integer retention, String manipulation, String course, String expect) {}
        List<Case> bad = List.of(
                new Case("字".repeat(31), 25, "平补平泻", "每周 2 次", "针法限 30 字内"),
                new Case("毫针", 0, "平补平泻", "每周 2 次", "留针时长无效"),
                new Case("毫针", 241, "平补平泻", "每周 2 次", "留针时长无效"),
                new Case("毫针", 25, "字".repeat(31), "每周 2 次", "手法限 30 字内"),
                new Case("毫针", 25, "平补平泻", "字".repeat(101), "疗程限 100 字内"));
        int before = treatRepository.findAll().size();
        for (Case c : bad) {
            TreatView v = view(null, "主诉", "方案");
            v.setAcuMethod(c.acuMethod());
            v.setRetentionMinutes(c.retention());
            v.setManipulation(c.manipulation());
            v.setAcuCourse(c.course());
            var e = assertThrows(IllegalArgumentException.class,
                    () -> treatService.save(v.ToTreatEntity()), c.expect());
            assertTrue(e.getMessage().contains(c.expect()), c.expect() + " ← " + e.getMessage());
        }
        assertEquals(before, treatRepository.findAll().size(), "守卫拒绝后不应落单");
    }
}
