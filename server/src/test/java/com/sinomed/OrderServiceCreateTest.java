package com.sinomed;

import com.sinomed.entity.CustomerEntity;
import com.sinomed.entity.ItemEntity;
import com.sinomed.entity.StaffEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.ItemRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 前台建单（OrderService.create）回归：卡项存在/上架校验、价格快照、status=0、建单人落库。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/order-create-test-${random.uuid}.db"
})
class OrderServiceCreateTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffRepository staffRepository;

    private static Long customerId;
    private static Long staffId;
    private static Long itemId;
    private static Long disabledItemId;

    @BeforeEach
    void seedData() {
        if (itemId == null) {
            CustomerEntity customer = new CustomerEntity();
            customer.setName("建档顾客");
            customer.setPhone("13900003001");
            customerId = customerRepository.save(customer).getId();

            StaffEntity staff = new StaffEntity();
            staff.setName("建单员工");
            staff.setAccount("order-test-staff");
            staff.setStatus(1);
            staffId = staffRepository.save(staff).getId();

            ItemEntity item = new ItemEntity();
            item.setName("前台建单卡项");
            item.setDescription("");
            item.setPrice(288);
            item.setEnabled(1);
            item.setSort(0);
            itemId = itemRepository.save(item).getId();

            ItemEntity disabled = new ItemEntity();
            disabled.setName("前台建单下架项");
            disabled.setDescription("");
            disabled.setPrice(66);
            disabled.setEnabled(0);
            disabled.setSort(0);
            disabledItemId = itemRepository.save(disabled).getId();
        }
    }

    @Test
    void createRecordsPriceSnapshotStatusAndStaff() {
        var order = orderService.create(customerId, itemId, staffId);
        assertEquals(0, order.getStatus());
        assertEquals(288, order.getPrice());
        assertEquals(customerId, order.getUserId());
        assertEquals(itemId, order.getItemId());
        assertEquals(staffId, order.getStaffId());
    }

    @Test
    void createWithoutStaffKeepsStaffNull() {
        var order = orderService.create(customerId, itemId, null);
        assertEquals(0, order.getStatus());
        assertEquals(null, order.getStaffId());
    }

    @Test
    void nonexistentItemRejected() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> orderService.create(customerId, 99999L, null));
        assertTrue(e.getMessage().startsWith("卡项不存在"));
    }

    @Test
    void disabledItemRejected() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> orderService.create(customerId, disabledItemId, null));
        assertTrue(e.getMessage().startsWith("卡项已下架"));
    }
}
