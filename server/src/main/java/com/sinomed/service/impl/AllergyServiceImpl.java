package com.sinomed.service.impl;

import com.sinomed.entity.CustomerHistoryEntity;
import com.sinomed.repository.CustomerHistoryRepository;
import com.sinomed.repository.CustomerRepository;
import com.sinomed.service.AllergyService;
import com.sinomed.vo.AllergyResultView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 过敏审方实现：顾客过敏史为自由文本，原文包含药名即命中（如「阿胶、蜂蜜过敏」命中「阿胶」）。
 * 口径：提示不拦截——命中的处方仍可开方，由医师判断；药名不足 2 字不参与匹配（避免「参」这类
 * 单字在长文本里误报）。审方只读过敏史（type=0），既往史不参与。
 */
@Service
@RequiredArgsConstructor
public class AllergyServiceImpl implements AllergyService {

    /** 单字药名在过敏史原文里误报率高，不参与匹配 */
    private static final int MIN_HERB_LENGTH = 2;

    private final CustomerRepository customerRepository;
    private final CustomerHistoryRepository historyRepository;

    @Override
    public AllergyResultView check(Long customerId, List<String> herbNames) {
        if (customerId == null) {
            throw new IllegalArgumentException("请先定位顾客再比对过敏史");
        }
        customerRepository.findById(customerId).orElseThrow(() ->
                new IllegalArgumentException("顾客不存在：" + customerId));
        List<String> herbs = new ArrayList<>();
        if (herbNames != null) {
            for (String name : herbNames) {
                if (name != null && !name.isBlank()) {
                    herbs.add(name.trim());
                }
            }
        }
        if (herbs.isEmpty()) {
            throw new IllegalArgumentException("药材名单不能为空");
        }
        // 只取过敏史（type=0），新记录在前
        List<CustomerHistoryEntity> allergies = historyRepository
                .findByCustomerIdOrderByIdDesc(customerId).stream()
                .filter(h -> Integer.valueOf(0).equals(h.getType()))
                .toList();

        List<AllergyResultView.Finding> findings = new ArrayList<>();
        for (String herb : herbs) {
            if (herb.length() < MIN_HERB_LENGTH) {
                continue;
            }
            for (CustomerHistoryEntity allergy : allergies) {
                if (allergy.getContent() != null && allergy.getContent().contains(herb)) {
                    findings.add(AllergyResultView.Finding.builder()
                            .herb(herb)
                            .historyId(allergy.getId())
                            .content(allergy.getContent())
                            .build());
                }
            }
        }
        return AllergyResultView.builder()
                .customerId(customerId)
                .checked(herbs.size())
                .findings(findings)
                .build();
    }
}
