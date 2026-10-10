package com.sinomed.service;

import com.sinomed.vo.PrescriptionItemView;
import com.sinomed.vo.PricingView;

import java.util.List;

/**
 * 处方计价：按药材字典对药味比价。
 */
public interface PricingService {

    /**
     * 实时计价（不落库）：字典按药名精确同名匹配；未收录药名列入 unknownHerbs 不计费。
     * 总价 = Σ round(每克分价 × 单剂克数) × 剂数；doses 空按 7。
     */
    PricingView price(List<PrescriptionItemView> herbs, Integer doses);
}
