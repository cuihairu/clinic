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
 * 膏方领取（PrescriptionService.save 膏方落库 + setPasteStatus 流转）回归：
 * prescriptionType=1 落「待制作」、craft 记收膏方式、非膏方「无需领取流转」；
 * 流转只允许 待制作→可取→已取。库路径随机文件避免污染。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/prescription-paste-test-${random.uuid}.db"
})
class PrescriptionPasteTest {

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
            customer.setName("膏方顾客");
            customer.setPhone("13900008002");
            customerId = customerRepository.save(customer).getId();
        }
    }

    private Long create(Integer prescriptionType, String craft, boolean decoction) {
        PrescriptionItemView herb = new PrescriptionItemView();
        herb.setHerb("熟地黄");
        herb.setWeight(60.0);
        PrescriptionView view = PrescriptionView.builder()
                .customerId(customerId)
                .doses(30)
                .prescriptionType(prescriptionType)
                .craft(craft)
                .decoction(decoction)
                .herbs(List.of(herb))
                .build();
        return prescriptionService.save(view).getId();
    }

    @Test
    void createPastePrescriptionSetsPendingAndCraft() {
        Long id = create(1, "炼蜜", false);
        var entity = prescriptionRepository.findById(id).orElseThrow();
        assertEquals(1, entity.getPrescriptionType());
        assertEquals(1, entity.getPasteStatus(), "膏方开方即落「待制作」");
        assertEquals(0, entity.getDecoctionStatus(), "膏方与代煎互不占用");
        assertEquals("炼蜜", entity.getCraft());

        PrescriptionView view = prescriptionService.findById(id);
        assertEquals(1, view.getPrescriptionType());
        assertEquals(1, view.getPasteStatus());
        assertEquals("炼蜜", view.getCraft());
    }

    @Test
    void createSoupPrescriptionStaysNonPaste() {
        Long id = create(null, "炼蜜", false);
        var entity = prescriptionRepository.findById(id).orElseThrow();
        assertEquals(0, entity.getPrescriptionType());
        assertEquals(0, entity.getPasteStatus());
        assertNull(entity.getCraft(), "非膏方不记收膏方式");
    }

    @Test
    void pasteFlowsPendingReadyTaken() {
        Long id = create(1, "清膏", false);
        assertEquals(2, prescriptionService.setPasteStatus(id, 2).getPasteStatus());
        assertEquals(3, prescriptionService.setPasteStatus(id, 3).getPasteStatus());
        // 已取后再推进被拦
        var e = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setPasteStatus(id, 3));
        assertTrue(e.getMessage().contains("流转无效"));
    }

    @Test
    void pasteFlowRejectsSkipAndNone() {
        Long none = create(0, null, false);
        var e0 = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setPasteStatus(none, 2));
        assertTrue(e0.getMessage().contains("不是膏方"));

        Long pending = create(1, "炼蜜", false);
        var e1 = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setPasteStatus(pending, 3));
        assertTrue(e1.getMessage().contains("流转无效"), "待制作不能直接跳到已取");

        var e2 = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setPasteStatus(pending, 1));
        assertTrue(e2.getMessage().contains("膏方状态无效"));
        assertEquals(1, prescriptionRepository.findById(pending).orElseThrow().getPasteStatus(), "失败流转不改状态");
    }

    @Test
    void pasteFlowRejectsUnknownPrescription() {
        var e = assertThrows(IllegalArgumentException.class, () -> prescriptionService.setPasteStatus(99999L, 2));
        assertEquals("处方不存在：99999", e.getMessage());
    }
}
