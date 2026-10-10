package com.sinomed;

import com.sinomed.entity.CustomerEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.PrescriptionRepository;
import com.sinomed.service.PrescriptionService;
import com.sinomed.vo.PrescriptionItemView;
import com.sinomed.vo.PrescriptionView;
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
 * 代煎领取（PrescriptionService.save 代煎落库 + setDecoctionStatus 流转）回归：
 * decoction=true 袋数=剂数落「待煎」、否则「无需代煎」；流转只允许 待煎→可取→已取；
 * 服务费=袋数×300 分实时算不落库。库路径随机文件避免污染。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/prescription-decoction-test-${random.uuid}.db"
})
class PrescriptionDecoctionTest {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private static Long customerId;

    @BeforeEach
    void seedCustomer() {
        if (customerId == null) {
            CustomerEntity customer = new CustomerEntity();
            customer.setName("代煎顾客");
            customer.setPhone("13900008001");
            customerId = customerRepository.save(customer).getId();
        }
    }

    private Long create(boolean decoction, int doses) {
        PrescriptionItemView herb = new PrescriptionItemView();
        herb.setHerb("甘草");
        herb.setWeight(6.0);
        PrescriptionView view = PrescriptionView.builder()
                .customerId(customerId)
                .doses(doses)
                .decoction(decoction)
                .herbs(List.of(herb))
                .build();
        return prescriptionService.save(view).getId();
    }

    @Test
    void createWithDecoctionSetsPendingAndBagsFromDoses() {
        Long id = create(true, 7);
        var entity = prescriptionRepository.findById(id).orElseThrow();
        assertEquals(1, entity.getDecoctionStatus());
        assertEquals(7, entity.getDecoctionBags());

        PrescriptionView view = prescriptionService.findById(id);
        assertEquals(1, view.getDecoctionStatus());
        assertEquals(7, view.getDecoctionBags());
        assertEquals(2100, view.getDecoctionFeeFen(), "服务费 = 7 袋 × 300 分");
    }

    @Test
    void createWithoutDecoctionStaysNone() {
        Long id = create(false, 5);
        var entity = prescriptionRepository.findById(id).orElseThrow();
        assertEquals(0, entity.getDecoctionStatus());
        assertNull(entity.getDecoctionBags());
        assertNull(prescriptionService.findById(id).getDecoctionFeeFen());
    }

    @Test
    void decoctionFlowsPendingReadyTaken() {
        Long id = create(true, 7);
        assertEquals(2, prescriptionService.setDecoctionStatus(id, 2).getDecoctionStatus());
        assertEquals(3, prescriptionService.setDecoctionStatus(id, 3).getDecoctionStatus());
        // 已取后再推进被拦
        var e = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setDecoctionStatus(id, 3));
        assertTrue(e.getMessage().contains("流转无效"));
    }

    @Test
    void decoctionFlowRejectsSkipAndNone() {
        Long none = create(false, 7);
        var e0 = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setDecoctionStatus(none, 2));
        assertTrue(e0.getMessage().contains("未选代煎"));

        Long pending = create(true, 7);
        var e1 = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setDecoctionStatus(pending, 3));
        assertTrue(e1.getMessage().contains("流转无效"), "待煎不能直接跳到已取");

        var e2 = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setDecoctionStatus(pending, 4));
        assertTrue(e2.getMessage().contains("代煎状态无效"));
        assertEquals(1, prescriptionRepository.findById(pending).orElseThrow().getDecoctionStatus(), "失败流转不改状态");
    }

    @Test
    void decoctionFlowRejectsUnknownPrescription() {
        var e = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setDecoctionStatus(99999L, 2));
        assertEquals("处方不存在：99999", e.getMessage());
    }
}
