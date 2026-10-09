package com.sinomed.service;

import com.sinomed.entity.RechargeEntity;
import com.sinomed.vo.RechargeBalanceView;
import com.sinomed.vo.RechargeView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RechargeService {
    /**
     * 储值充值：落一条正数流水
     */
    RechargeEntity recharge(RechargeView view);

    /**
     * 查顾客储值余额（流水合计，无流水为 0）
     */
    RechargeBalanceView balance(Long customerId);

    /**
     * 储值流水分页；customerId 为空查全部
     */
    Page<RechargeEntity> findPage(Long customerId, Pageable pageable);
}
