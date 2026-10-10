package com.sinomed;

import com.sinomed.entity.SettlementEntity;
import com.sinomed.repository.SettlementRepository;
import com.sinomed.service.SettlementService;
import com.sinomed.vo.SettlementMonthReportView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 月度收费报表：按结算时间聚合逐日/支付方式，月外结算单不纳入，
 * 次卡核销（payType 5）计入单数、实收为 0；月份格式非法报 400。
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:sqlite:target/settle-report-test-${random.uuid}.db"})
@Transactional
class SettlementReportTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    /** 建结算单并把结算时间回写到指定日期（审计只写当下，走原生 SQL 回写） */
    private void newSettlement(long orderId, int payType, int money, LocalDate day) {
        SettlementEntity settlement = new SettlementEntity();
        settlement.setOrderId(orderId);
        settlement.setUserId(orderId);
        settlement.setPayType(payType);
        settlement.setMoney(money);
        settlementRepository.saveAndFlush(settlement);
        Date when = Date.from(day.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant());
        jdbcTemplate.update("UPDATE settlements SET create_time = ?, update_time = ? WHERE id = ?",
                new Timestamp(when.getTime()), new Timestamp(when.getTime()), settlement.getId());
        entityManager.clear(); // 让后续查询从库里重读回写后的时间
    }

    @Test
    void monthReportAggregatesDaysAndPayTypes() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        String month = monthStart.toString().substring(0, 7);

        newSettlement(1, 2, 300, monthStart);              // 微信
        newSettlement(2, 4, 100, monthStart);              // 现金
        newSettlement(3, 5, 0, monthStart.plusDays(1));    // 次卡核销，实收 0
        newSettlement(4, 2, 500, monthStart.minusDays(1)); // 上月，不纳入

        SettlementMonthReportView report = settlementService.monthReport(month);
        assertEquals(month, report.getMonth());
        assertEquals(3, report.getTotalCount(), "上月单不计入：" + report);
        assertEquals(400, report.getTotalMoney());
        assertEquals(1, report.getCardCount());

        assertEquals(2, report.getDays().size(), "只有有结算的日期：" + report.getDays());
        SettlementMonthReportView.DayRow firstDay = report.getDays().get(0);
        assertEquals(monthStart.toString(), firstDay.getDay());
        assertEquals(2, firstDay.getCount());
        assertEquals(400, firstDay.getMoney());
        SettlementMonthReportView.DayRow secondDay = report.getDays().get(1);
        assertEquals(monthStart.plusDays(1).toString(), secondDay.getDay());
        assertEquals(0, secondDay.getMoney());

        assertEquals(3, report.getPayTypes().size(), "微信/现金/次卡各一组：" + report.getPayTypes());
        assertEquals("微信", report.getPayTypes().get(0).getPayTypeText());
        assertEquals(300, report.getPayTypes().get(0).getMoney());
        assertEquals("次卡抵扣", report.getPayTypes().get(2).getPayTypeText());
        assertEquals(0, report.getPayTypes().get(2).getMoney());
    }

    @Test
    void monthReportRejectsInvalidMonth() {
        for (String bad : new String[] {null, "abc", "202610", "2026-1"}) {
            try {
                settlementService.monthReport(bad);
                throw new AssertionError("非法月份应报 400：" + bad);
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("报表月份格式无效"), "应提示格式无效：" + e.getMessage());
            }
        }
        try {
            settlementService.monthReport("2026-13");
            throw new AssertionError("不存在月份应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("报表月份无效"), "应提示月份无效：" + e.getMessage());
        }
    }
}
