package com.sinomed;

import com.sinomed.entity.CustomerCardEntity;
import com.sinomed.entity.CustomerEntity;
import com.sinomed.entity.ItemEntity;
import com.sinomed.entity.OrderEntity;
import com.sinomed.repository.CustomerCardRepository;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.ItemRepository;
import com.sinomed.repository.OrderRepository;
import com.sinomed.service.CardService;
import com.sinomed.service.SettlementService;
import com.sinomed.vo.CardView;
import com.sinomed.vo.SettlementView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 次卡抵扣结算（SettlementService payType 5）回归：有卡扣 1 次实收 0、
 * 无卡报 400。库路径随机文件避免污染。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/settlement-card-test-${random.uuid}.db"
})
class SettlementCardTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private CardService cardService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerCardRepository cardRepository;

    private static Long itemId;

    @BeforeEach
    void seed() {
        if (itemId == null) {
            ItemEntity item = new ItemEntity();
            item.setName("结算次卡项");
            item.setDescription("");
            item.setPrice(680);
            item.setEnabled(1);
            item.setSort(0);
            itemId = itemRepository.save(item).getId();
        }
    }

    /** 每个用例独立顾客，避免方法执行顺序耦合 */
    private Long newCustomer(String phone) {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("结算顾客");
        customer.setPhone(phone);
        return customerRepository.save(customer).getId();
    }

    private Long newOrder(Long customer, int price) {
        OrderEntity order = new OrderEntity();
        order.setUserId(customer);
        order.setItemId(itemId);
        order.setStatus(0);
        order.setPrice(price);
        return orderRepository.save(order).getId();
    }

    @Test
    void settleWithCardDeductsOnceAndPaysZero() {
        Long customer = newCustomer("13900006011");
        Long orderId = newOrder(customer, 680);
        cardService.issue(CardView.builder().customerId(customer).itemId(itemId).totalTimes(10).build());

        var settlement = settlementService.settle(SettlementView.builder().orderId(orderId).payType(5).build());
        assertEquals(0, settlement.getMoney());
        assertEquals(5, settlement.getPayType());
        assertEquals(2, orderRepository.findById(orderId).orElseThrow().getStatus());
        var cards = cardRepository.findByCustomerIdOrderByIdDesc(customer);
        assertEquals(1, cards.size());
        assertEquals(9, cards.get(0).getRemainingTimes());
    }

    @Test
    void settleWithCardWithoutCardRejected() {
        Long customer = newCustomer("13900006012");
        Long orderId = newOrder(customer, 680);
        var e = assertThrows(IllegalArgumentException.class,
                () -> settlementService.settle(SettlementView.builder().orderId(orderId).payType(5).build()));
        assertTrue(e.getMessage().contains("无可抵扣次卡"));
        // 订单状态不变（事务回滚）
        assertEquals(0, orderRepository.findById(orderId).orElseThrow().getStatus());
    }

    @Test
    void settleWithCardOnlyDeductsOncePerSettlement() {
        Long customer = newCustomer("13900006013");
        Long orderId = newOrder(customer, 680);
        cardService.issue(CardView.builder().customerId(customer).itemId(itemId).totalTimes(10).build());
        settlementService.settle(SettlementView.builder().orderId(orderId).payType(5).build());
        // 同单再结算被拦（完结/已结算校验），不会二次扣卡
        assertThrows(IllegalArgumentException.class,
                () -> settlementService.settle(SettlementView.builder().orderId(orderId).payType(5).build()));
        assertEquals(9, cardRepository.findByCustomerIdOrderByIdDesc(customer).get(0).getRemainingTimes());
    }
}
