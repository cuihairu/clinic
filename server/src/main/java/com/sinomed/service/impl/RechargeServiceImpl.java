package com.sinomed.service.impl;

import com.sinomed.entity.RechargeEntity;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.repository.RechargeRepository;
import com.sinomed.service.RechargeService;
import com.sinomed.vo.RechargeBalanceView;
import com.sinomed.vo.RechargeView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RechargeServiceImpl implements RechargeService {

    private final RechargeRepository rechargeRepository;
    private final CustomerRepository customerRepository;

    /**
     * 储值充值：金额必须为正；顾客须已建档；创建/更新时间由审计维护
     */
    @Override
    public RechargeEntity recharge(RechargeView view) {
        if (view.getCustomerId() == null) {
            throw new IllegalArgumentException("顾客id不能为空");
        }
        if (view.getMoney() == null || view.getMoney() <= 0) {
            throw new IllegalArgumentException("充值金额必须大于 0");
        }
        customerRepository.findById(view.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("顾客不存在：" + view.getCustomerId()));
        RechargeEntity recharge = new RechargeEntity();
        recharge.setUserId(view.getCustomerId());
        recharge.setMoney(view.getMoney());
        return rechargeRepository.save(recharge);
    }

    /**
     * 按顾客汇总储值：余额 = 流水合计（充值为正、储值支付为负），无流水为 0
     */
    @Override
    public RechargeBalanceView balance(Long customerId) {
        return RechargeBalanceView.builder()
                .customerId(customerId)
                .balance(rechargeRepository.sumMoneyByUserId(customerId))
                .build();
    }

    /**
     * 储值流水分页；customerId 为空查全部
     */
    @Override
    public Page<RechargeEntity> findPage(Long customerId, Pageable pageable) {
        if (customerId == null) {
            return rechargeRepository.findAll(pageable);
        }
        return rechargeRepository.findByUserId(customerId, pageable);
    }
}
