package com.sinomed.service.impl;

import com.sinomed.entity.HerbEntity;
import com.sinomed.repository.HerbRepository;
import com.sinomed.service.PricingService;
import com.sinomed.vo.PrescriptionItemView;
import com.sinomed.vo.PricingView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 计价实现：字典精确同名匹配（不做包含猜测——钱的事要确定），
 * 未收录药名如实列出；价格只依赖字典当前值，不做快照。
 */
@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private static final int DEFAULT_DOSES = 7;

    private final HerbRepository herbRepository;

    @Override
    public PricingView price(List<PrescriptionItemView> herbs, Integer doses) {
        int doseCount = doses == null || doses < 1 ? DEFAULT_DOSES : doses;
        List<PrescriptionItemView> named = new ArrayList<>();
        if (herbs != null) {
            for (PrescriptionItemView herb : herbs) {
                if (herb.getHerb() != null && !herb.getHerb().isBlank()) {
                    named.add(herb);
                }
            }
        }

        Map<String, HerbEntity> dict = named.isEmpty() ? Map.of()
                : herbRepository.findByNameIn(named.stream()
                        .map(h -> h.getHerb().trim()).collect(Collectors.toSet())).stream()
                        .collect(Collectors.toMap(HerbEntity::getName, Function.identity()));

        long perDoseFen = 0;
        List<String> unknown = new ArrayList<>();
        int pricedCount = 0;
        for (PrescriptionItemView herb : named) {
            String name = herb.getHerb().trim();
            HerbEntity entry = dict.get(name);
            if (entry == null) {
                unknown.add(name);
                continue;
            }
            pricedCount++;
            double weight = herb.getWeight() == null ? 0 : herb.getWeight();
            perDoseFen += Math.round(entry.getPrice() * weight);
        }
        return PricingView.builder()
                .totalFen(perDoseFen * doseCount)
                .perDoseFen(perDoseFen)
                .doses(doseCount)
                .herbCount(named.size())
                .pricedHerbCount(pricedCount)
                .unknownHerbs(unknown)
                .build();
    }
}
