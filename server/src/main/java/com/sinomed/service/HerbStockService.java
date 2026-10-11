package com.sinomed.service;

import com.sinomed.vo.HerbStockBalanceView;
import com.sinomed.vo.HerbStockLogView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 饮片出入库台账：流水登记（入库/出库）、余额与近期到期预警。
 */
public interface HerbStockService {

    /** 登记一条出入库流水（入库 1 / 出库 0）；出库不得超当前库存 */
    HerbStockLogView addLog(HerbStockLogView view);

    /** 流水分页：herbId/type 可空过滤（按空值分派），联出药材名，id 倒序 */
    Page<HerbStockLogView> findPage(Long herbId, Integer type, Pageable pageable);

    /** 各药材当前库存（克）+ 最早未消耗批次效期；expiryWithinDays 内到期/已过期标 warnExpiry */
    List<HerbStockBalanceView> balance(Integer expiryWithinDays);

    /** 删除一条流水（演示口径，无留痕；删除后余额按剩余流水重算）；返回被删流水（联药材名） */
    HerbStockLogView deleteById(Long id);
}
