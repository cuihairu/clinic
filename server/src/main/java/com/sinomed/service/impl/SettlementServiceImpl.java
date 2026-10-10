package com.sinomed.service.impl;

import com.sinomed.entity.OrderEntity;
import com.sinomed.entity.RechargeEntity;
import com.sinomed.entity.SettlementEntity;
import com.sinomed.repository.OrderRepository;
import com.sinomed.repository.RechargeRepository;
import com.sinomed.repository.SettlementRepository;
import com.sinomed.service.CardService;
import com.sinomed.service.SettlementService;
import com.sinomed.vo.SettlementMonthReportView;
import com.sinomed.vo.SettlementMonthReportView.DayRow;
import com.sinomed.vo.SettlementMonthReportView.PayRow;
import com.sinomed.vo.SettlementView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettlementServiceImpl implements SettlementService {

    /** 支付方式：1 储值 / 2 微信 / 3 支付宝 / 4 现金 / 5 次卡抵扣 */
    static final int PAY_STORED_VALUE = 1;
    static final int PAY_WECHAT = 2;
    static final int PAY_ALIPAY = 3;
    static final int PAY_CASH = 4;
    static final int PAY_CARD = 5;

    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;
    private final RechargeRepository rechargeRepository;
    private final CardService cardService;

    /**
     * 收费结算：订单 0 已下单 / 1 已确认 → 2 已完成；落一条结算单（一单一结算，order_id 唯一兜底）。
     * 储值支付先校验余额，足够则落一条负数流水扣减。金额取订单价格快照，空价格按 0 收。
     * 次卡抵扣（payType 5）：订单卡项须有有效余次卡，扣 1 次，实收记 0（money=实收口径）。
     */
    @Override
    @Transactional
    public SettlementEntity settle(SettlementView view) {
        if (view.getOrderId() == null) {
            throw new IllegalArgumentException("订单id不能为空");
        }
        int payType = view.getPayType() == null ? 0 : view.getPayType();
        if (payType < PAY_STORED_VALUE || payType > PAY_CARD) {
            throw new IllegalArgumentException("支付方式无效：" + view.getPayType()
                    + "（1 储值 / 2 微信 / 3 支付宝 / 4 现金 / 5 次卡抵扣）");
        }
        OrderEntity order = orderRepository.findById(view.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在：" + view.getOrderId()));
        Integer current = order.getStatus();
        if (current == null || current == 2 || current == 9) {
            throw new IllegalArgumentException("订单状态 " + current + " 已完结，不能结算");
        }
        if (settlementRepository.findByOrderId(view.getOrderId()).isPresent()) {
            throw new IllegalArgumentException("订单已结算过：" + view.getOrderId());
        }
        int price = order.getPrice() == null ? 0 : order.getPrice();

        int paid = price;
        if (payType == PAY_STORED_VALUE) {
            long balance = rechargeRepository.sumMoneyByUserId(order.getUserId());
            if (balance < price) {
                throw new IllegalArgumentException("储值余额不足（余额 " + balance + " 元，应收 " + price + " 元），请先充值或改用其他支付");
            }
            RechargeEntity deduct = new RechargeEntity();
            deduct.setUserId(order.getUserId());
            deduct.setMoney(-price);
            rechargeRepository.save(deduct);
        } else if (payType == PAY_CARD) {
            cardService.deduct(order.getUserId(), order.getItemId(), order.getId());
            paid = 0;
        }

        SettlementEntity settlement = new SettlementEntity();
        settlement.setOrderId(order.getId());
        settlement.setUserId(order.getUserId());
        settlement.setPayType(payType);
        settlement.setMoney(paid);
        settlement = settlementRepository.save(settlement);

        order.setStatus(2);
        orderRepository.save(order);
        return settlement;
    }

    /**
     * 结算单分页
     */
    @Override
    public Page<SettlementEntity> findPage(Pageable pageable) {
        return settlementRepository.findAll(pageable);
    }

    /** 报表月份格式：yyyy-MM */
    static final Pattern MONTH_PATTERN = Pattern.compile("\\d{4}-\\d{2}");

    /**
     * 月度收费报表：按结算时间取当月结算单（[月初, 下月初)），Java 内聚合
     * （SQLite 方言不便做日期分组）。次卡核销计入单数、实收为 0；
     * 支付方式构成只列出现过的，按 payType 升序。
     */
    @Override
    public SettlementMonthReportView monthReport(String month) {
        if (month == null || !MONTH_PATTERN.matcher(month).matches()) {
            throw new IllegalArgumentException("报表月份格式无效：" + month + "（应为 yyyy-MM）");
        }
        LocalDate firstDay;
        try {
            firstDay = LocalDate.parse(month + "-01");
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("报表月份无效：" + month);
        }
        Date start = Date.from(firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(firstDay.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<SettlementEntity> rows = settlementRepository.findByCreateTimeBetween(start, end);

        // day -> [单数, 实收]；payType -> [单数, 实收]，TreeMap 保证升序
        Map<String, int[]> byDay = new TreeMap<>();
        Map<Integer, int[]> byPay = new TreeMap<>();
        int cardCount = 0;
        for (SettlementEntity row : rows) {
            int money = row.getMoney() == null ? 0 : row.getMoney();
            Date time = row.getCreateTime();
            if (time != null) {
                String day = LocalDate.ofInstant(time.toInstant(), ZoneId.systemDefault()).toString();
                int[] dayAgg = byDay.computeIfAbsent(day, k -> new int[2]);
                dayAgg[0]++;
                dayAgg[1] += money;
            }
            int payType = row.getPayType() == null ? 0 : row.getPayType();
            int[] payAgg = byPay.computeIfAbsent(payType, k -> new int[2]);
            payAgg[0]++;
            payAgg[1] += money;
            if (payType == PAY_CARD) {
                cardCount++;
            }
        }
        List<DayRow> days = byDay.entrySet().stream()
                .map(e -> DayRow.builder().day(e.getKey()).count(e.getValue()[0]).money(e.getValue()[1]).build())
                .toList();
        List<PayRow> payTypes = byPay.entrySet().stream()
                .map(e -> PayRow.builder().payType(e.getKey()).payTypeText(payTypeText(e.getKey()))
                        .count(e.getValue()[0]).money(e.getValue()[1]).build())
                .toList();
        return SettlementMonthReportView.builder()
                .month(month)
                .totalCount(rows.size())
                .totalMoney(rows.stream().mapToInt(r -> r.getMoney() == null ? 0 : r.getMoney()).sum())
                .cardCount(cardCount)
                .days(days)
                .payTypes(payTypes)
                .build();
    }

    private static String payTypeText(int payType) {
        return switch (payType) {
            case PAY_STORED_VALUE -> "储值";
            case PAY_WECHAT -> "微信";
            case PAY_ALIPAY -> "支付宝";
            case PAY_CASH -> "现金";
            case PAY_CARD -> "次卡抵扣";
            default -> "未知(" + payType + ")";
        };
    }
}
