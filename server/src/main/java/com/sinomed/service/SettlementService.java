package com.sinomed.service;

import com.sinomed.entity.SettlementEntity;
import com.sinomed.vo.SettlementMonthReportView;
import com.sinomed.vo.SettlementView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementService {
    /**
     * 收费结算：订单状态 0/1 → 2 已完成，落一单结算记录（一单一结算）；
     * 储值支付先校验余额并落负数流水。整个动作一个事务。
     */
    SettlementEntity settle(SettlementView view);

    /**
     * 结算单分页（时间倒序由调用方排序）
     */
    Page<SettlementEntity> findPage(Pageable pageable);

    /**
     * 月度收费报表：按结算时间取当月结算单，聚合逐日与支付方式构成；
     * 月份格式非 yyyy-MM 报 400。
     */
    SettlementMonthReportView monthReport(String month);
}
