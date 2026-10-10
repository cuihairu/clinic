package com.sinomed.service;

import com.sinomed.entity.CustomerCardEntity;
import com.sinomed.vo.CardView;

import java.util.List;

/**
 * 顾客持卡（次卡）：发卡、按顾客查询、停用/恢复、结算抵扣。
 */
public interface CardService {

    /** 发卡：顾客与卡项必须存在，总次数 ≥ 1，余次=总次数、状态有效 */
    CustomerCardEntity issue(CardView view);

    /** 按顾客查持卡（新卡在前，可空列表） */
    List<CustomerCardEntity> listByCustomer(Long customerId);

    /** 停用 / 恢复：status 1 有效 / 0 停用 */
    CustomerCardEntity setStatus(Long id, Integer status);

    /**
     * 结算抵扣：按顾客 + 卡项找有效且有余次的最早一张卡扣 1 次；
     * 无可抵扣卡抛 IllegalArgumentException（转 400）。
     */
    CustomerCardEntity deduct(Long customerId, Long itemId);
}
