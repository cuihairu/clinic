package com.sinomed;

import com.sinomed.entity.CustomerEntity;
import com.sinomed.entity.CustomerHistoryEntity;
import com.sinomed.repository.CustomerHistoryRepository;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.service.AllergyService;
import com.sinomed.vo.AllergyResultView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 过敏审方（提示不拦截）：顾客过敏史原文包含药名即命中（≥2 字），既往史不参与，
 * 单字药名不匹配；顾客不存在/药味名单为空报 400。
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:sqlite:target/allergy-test-${random.uuid}.db"})
@Transactional
class PrescriptionAllergyTest {

    @Autowired
    private AllergyService allergyService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerHistoryRepository historyRepository;

    private Long newCustomer(String name) {
        CustomerEntity customer = new CustomerEntity();
        customer.setName(name);
        customer.setGender(1);
        return customerRepository.save(customer).getId();
    }

    private void addAllergy(Long customerId, String content) {
        CustomerHistoryEntity history = new CustomerHistoryEntity();
        history.setCustomerId(customerId);
        history.setType(0);
        history.setContent(content);
        historyRepository.save(history);
    }

    @Test
    void contentContainingHerbNameHits() {
        Long customerId = newCustomer("过敏审方顾客");
        addAllergy(customerId, "阿胶、蜂蜜过敏");
        addAllergy(customerId, "海鲜类食物过敏");

        AllergyResultView result = allergyService.check(customerId, List.of("熟地黄", "阿胶", "砂仁"));
        assertEquals(3, result.getChecked());
        assertEquals(1, result.getFindings().size(), "只应命中阿胶：" + result.getFindings());
        AllergyResultView.Finding hit = result.getFindings().get(0);
        assertEquals("阿胶", hit.getHerb());
        assertEquals("阿胶、蜂蜜过敏", hit.getContent());
        assertTrue(hit.getHistoryId() != null, "命中应带过敏史记录id");
    }

    @Test
    void previousHistoryAndShortHerbsAreSkipped() {
        Long customerId = newCustomer("既往不参与顾客");
        // 既往史（type=1）即使提到药名也不命中
        CustomerHistoryEntity past = new CustomerHistoryEntity();
        past.setCustomerId(customerId);
        past.setType(1);
        past.setContent("曾服阿胶制品调理");
        historyRepository.save(past);
        // 单字药名（如「参」）不参与匹配，避免长文本误报

        AllergyResultView result = allergyService.check(customerId, List.of("阿胶", "参", "芪"));
        assertTrue(result.getFindings().isEmpty(), "既往史与单字药名都不应命中：" + result.getFindings());
        assertEquals(3, result.getChecked(), "单字药名仍计入比对数");
    }

    @Test
    void sameHerbReportsEachAllergyRow() {
        Long customerId = newCustomer("多行命中顾客");
        addAllergy(customerId, "阿胶过敏");
        addAllergy(customerId, "对阿胶及制品过敏");

        AllergyResultView result = allergyService.check(customerId, List.of("阿胶"));
        assertEquals(2, result.getFindings().size(), "两条过敏史都提到阿胶应各报一条：" + result.getFindings());
    }

    @Test
    void unknownCustomerAndBlankHerbsRejected() {
        try {
            allergyService.check(99999L, List.of("阿胶"));
            throw new AssertionError("未知顾客应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("顾客不存在"), "应提示顾客不存在：" + e.getMessage());
        }
        Long customerId = newCustomer("空名单顾客");
        try {
            allergyService.check(customerId, List.of("  "));
            throw new AssertionError("空白药味应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("药材名单不能为空"), "应提示名单为空：" + e.getMessage());
        }
    }
}
