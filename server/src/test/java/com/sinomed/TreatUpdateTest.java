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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        var e0 = assertThrows(IllegalArgumentException.class, () -> treatService.update(view(null, "x", "y")));
        assertEquals("接诊单id不能为空", e0.getMessage());

        var e1 = assertThrows(IllegalArgumentException.class, () -> treatService.update(view(99999L, "x", "y")));
        assertEquals("接诊单不存在：99999", e1.getMessage());
        assertTrue(treatRepository.findAll().isEmpty(), "失败更新不应落单");
    }
}
