package com.sinomed.service.impl;

import com.sinomed.entity.OrderEntity;
import com.sinomed.entity.RechargeEntity;
import com.sinomed.entity.SettlementEntity;
import com.sinomed.repository.OrderRepository;
import com.sinomed.repository.RechargeRepository;
import com.sinomed.repository.SettlementRepository;
import com.sinomed.service.CardService;
import com.sinomed.service.SettlementService;
import com.sinomed.vo.SettlementView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
