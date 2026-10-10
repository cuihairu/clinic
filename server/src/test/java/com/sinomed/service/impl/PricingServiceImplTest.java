package com.sinomed.service.impl;

import com.sinomed.entity.HerbEntity;
import com.sinomed.repository.HerbRepository;
import com.sinomed.vo.PrescriptionItemView;
import com.sinomed.vo.PricingView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 处方计价（PricingService.price）单测：精确同名比价、逐味取整、
 * 未收录药名如实列出不计费、默认剂数 7、空白药名跳过。
 */
class PricingServiceImplTest {

    private final Map<String, Integer> dict = new HashMap<>();
    private HerbRepository herbRepository;
    private PricingServiceImpl service;

    @BeforeEach
    void setUp() {
        herbRepository = mock(HerbRepository.class);
        service = new PricingServiceImpl(herbRepository);
        // 单点打桩：按当前 dict 内容应答（重复 when 会替换旧桩，故集中一处）
        when(herbRepository.findByNameIn(anyCollection())).thenAnswer(inv -> {
            Collection<String> asked = inv.getArgument(0);
            return dict.entrySet().stream()
                    .filter(e -> asked.contains(e.getKey()))
                    .map(e -> {
                        HerbEntity entry = new HerbEntity();
                        entry.setName(e.getKey());
                        entry.setPrice(e.getValue());
                        return entry;
                    })
                    .toList();
        });
    }

    private void dict(String name, int pricePerGram) {
        dict.put(name, pricePerGram);
    }

    @Test
    void pricesKnownHerbsWithDoses() {
        dict("黄芪", 6);
        dict("甘草", 2);
        // 6 分/g × 30g + 2 分/g × 6g = 192 分/剂；× 7 剂 = 1344 分
        var pricing = service.price(List.of(herb("黄芪", 30.0), herb("甘草", 6.0)), 7);
        assertEquals(192, pricing.getPerDoseFen());
        assertEquals(1344, pricing.getTotalFen());
        assertEquals(2, pricing.getHerbCount());
        assertEquals(2, pricing.getPricedHerbCount());
        assertTrue(pricing.getUnknownHerbs().isEmpty());
    }

    @Test
    void fractionalGramsRoundHalfUpPerHerb() {
        dict("当归", 5);
        // 5 分/g × 12.5g = 62.5 → 每味四舍五入 63 分
        var pricing = service.price(List.of(herb("当归", 12.5)), 1);
        assertEquals(63, pricing.getPerDoseFen());
    }

    @Test
    void unknownHerbsListedAndNotCharged() {
        dict("黄芪", 6);
        var pricing = service.price(List.of(herb("黄芪", 30.0), herb("独活", 9.0)), 7);
        assertEquals(180, pricing.getPerDoseFen());
        assertEquals(List.of("独活"), pricing.getUnknownHerbs());
        assertEquals(2, pricing.getHerbCount());
        assertEquals(1, pricing.getPricedHerbCount());
    }

    @Test
    void dosesDefaultToSevenWhenAbsent() {
        dict("甘草", 2);
        var pricing = service.price(List.of(herb("甘草", 6.0)), null);
        assertEquals(7, pricing.getDoses());
        assertEquals(84, pricing.getTotalFen());
    }

    @Test
    void blankHerbsSkippedAndEmptyOk() {
        dict("甘草", 2);
        var pricing = service.price(List.of(herb("  ", 6.0), herb("甘草", 6.0)), 3);
        assertEquals(1, pricing.getHerbCount());
        assertEquals(36, pricing.getTotalFen());
        var empty = service.price(List.of(), 7);
        assertEquals(0, empty.getHerbCount());
        assertEquals(0, empty.getTotalFen());
    }

    @Test
    void nullWeightCountsAsZeroAndStillPriced() {
        dict("甘草", 2);
        var pricing = service.price(List.of(herb("甘草", null)), 5);
        assertEquals(1, pricing.getPricedHerbCount());
        assertEquals(0, pricing.getTotalFen());
    }

    private PrescriptionItemView herb(String name, Double weight) {
        PrescriptionItemView view = new PrescriptionItemView();
        view.setHerb(name);
        view.setWeight(weight);
        return view;
    }
}
