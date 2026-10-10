package com.sinomed;

import com.sinomed.entity.CustomerEntity;
import com.sinomed.entity.ItemEntity;
import com.sinomed.entity.OrderEntity;
import com.sinomed.entity.StaffEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.ItemRepository;
import com.sinomed.repository.OrderRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.ReceiptPrintService;
import com.sinomed.service.SettlementService;
import com.sinomed.vo.SettlementView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 结算小票套打（ReceiptPrintService）回归：模板 token 全部填充、items 循环体
 * 正确替换、次卡抵扣金额列显示「次卡抵扣」、名字 HTML 转义、未知结算单报 400。
 * 库路径随机文件避免污染；结算用真实 settle 落单，保证与收款口径一致。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/receipt-print-test-${random.uuid}.db"
})
class ReceiptPrintTest {

    @Autowired
    private ReceiptPrintService receiptPrintService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private com.sinomed.service.CardService cardService;

    private static Long itemId;
    private static Long staffId;

    @BeforeEach
    void seed() {
        if (itemId == null) {
            ItemEntity item = new ItemEntity();
            item.setName("小票推拿项");
            item.setDescription("");
            item.setPrice(680);
            item.setEnabled(1);
            item.setSort(0);
            itemId = itemRepository.save(item).getId();

            StaffEntity staff = new StaffEntity();
            staff.setName("小票前台");
            staff.setAccount("receipt-staff");
            staff.setPassword("x");
            staff.setStatus(1);
            staffId = staffRepository.save(staff).getId();
        }
    }

    private Long newCustomer(String name, String phone) {
        CustomerEntity customer = new CustomerEntity();
        customer.setName(name);
        customer.setPhone(phone);
        return customerRepository.save(customer).getId();
    }

    private Long newOrder(Long customer, Long staff, int price) {
        OrderEntity order = new OrderEntity();
        order.setUserId(customer);
        order.setItemId(itemId);
        order.setStaffId(staff);
        order.setStatus(0);
        order.setPrice(price);
        return orderRepository.save(order).getId();
    }

    private Long settle(int payType, Long customer, Long staff) {
        Long orderId = newOrder(customer, staff, 680);
        return settlementService.settle(SettlementView.builder().orderId(orderId).payType(payType).build()).getId();
    }

    @Test
    void renderFillsAllTokensAndItemsRow() {
        Long settlementId = settle(4, newCustomer("张三丰", "13900007011"), staffId);
        String html = receiptPrintService.render(settlementId);

        assertTrue(html.contains("示例医馆"));
        assertTrue(html.contains("S" + settlementId), "单号应为 S+结算单id");
        assertTrue(html.contains("张三丰"));
        assertTrue(html.contains("小票推拿项 × 1"), "items 行应含项目名与数量");
        assertTrue(html.contains("¥680"));
        assertTrue(html.contains("现金"));
        assertTrue(html.contains("小票前台"), "经手人应联出员工名");
        assertFalse(html.contains("{{"), "所有 token 都应被填充");
        assertFalse(html.contains("BEGIN items"), "循环体标记不应残留");
    }

    @Test
    void renderCardPayShowsDeductionText() {
        Long customer = newCustomer("次卡顾客", "13900007012");
        cardService.issue(com.sinomed.vo.CardView.builder().customerId(customer).itemId(itemId).totalTimes(10).build());
        Long settlementId = settle(5, customer, null);
        String html = receiptPrintService.render(settlementId);

        assertTrue(html.contains("次卡抵扣"));
        assertTrue(html.contains(">¥0<"), "合计应为 ¥0（实收口径）");
        assertTrue(html.contains("—"), "无经手人时显示 —");
    }

    @Test
    void renderEscapesNameHtml() {
        Long settlementId = settle(4, newCustomer("<b>转义顾客</b>", "13900007013"), null);
        String html = receiptPrintService.render(settlementId);

        assertTrue(html.contains("&lt;b&gt;转义顾客&lt;/b&gt;"));
        assertFalse(html.contains("<b>转义顾客</b>"));
    }

    @Test
    void renderRejectsUnknownSettlement() {
        var e = assertThrows(IllegalArgumentException.class, () -> receiptPrintService.render(99999L));
        assertEquals("结算单不存在：99999", e.getMessage());
    }
}
