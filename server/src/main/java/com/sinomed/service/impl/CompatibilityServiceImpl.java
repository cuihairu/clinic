package com.sinomed.service.impl;

import com.sinomed.service.CompatibilityService;
import com.sinomed.vo.CompatibilityResultView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 配伍审方实现：经典十八反（禁忌）+ 十九畏（慎用）静态规则。
 * 口径：提示不拦截——命中的处方仍可开方，由医师判断；规则别名按包含匹配自由文本药名。
 */
@Service
public class CompatibilityServiceImpl implements CompatibilityService {

    private static final String LEVEL_FAN = "禁忌";
    private static final String LEVEL_WEI = "慎用";

    /** 一条规则：两侧别名任取其一同时出现即命中；sideA/sideB 顺序不敏感，按顺序归组展示 */
    private record Rule(String group, String level, String note, List<String> sideA, List<String> sideB) {
    }

    private static final List<Rule> RULES = List.of(
            // 十八反（禁忌）
            new Rule("十八反·乌头组", LEVEL_FAN, "半蒌贝蔹及攻乌",
                    List.of("乌头", "川乌", "草乌", "附子"),
                    List.of("半夏", "瓜蒌", "天花粉", "贝母", "白蔹", "白及")),
            new Rule("十八反·甘草组", LEVEL_FAN, "藻戟遂芫俱战草",
                    List.of("甘草"),
                    List.of("海藻", "大戟", "甘遂", "芫花")),
            new Rule("十八反·藜芦组", LEVEL_FAN, "诸参辛芍叛藜芦",
                    List.of("藜芦"),
                    List.of("人参", "党参", "丹参", "玄参", "沙参", "苦参", "细辛", "芍药", "白芍", "赤芍")),
            // 十九畏（慎用）
            new Rule("十九畏·硫黄朴硝", LEVEL_WEI, "硫黄畏朴硝",
                    List.of("硫黄"), List.of("朴硝", "芒硝")),
            new Rule("十九畏·水银砒霜", LEVEL_WEI, "水银畏砒霜",
                    List.of("水银"), List.of("砒霜")),
            new Rule("十九畏·狼毒密陀僧", LEVEL_WEI, "狼毒畏密陀僧",
                    List.of("狼毒"), List.of("密陀僧")),
            new Rule("十九畏·巴豆牵牛", LEVEL_WEI, "巴豆畏牵牛",
                    List.of("巴豆"), List.of("牵牛")),
            new Rule("十九畏·丁香郁金", LEVEL_WEI, "丁香畏郁金",
                    List.of("丁香"), List.of("郁金")),
            new Rule("十九畏·乌头犀角", LEVEL_WEI, "川乌草乌畏犀角（犀角已禁用，现用水牛角替代）",
                    List.of("乌头", "川乌", "草乌", "附子"), List.of("犀角")),
            new Rule("十九畏·牙硝三棱", LEVEL_WEI, "牙硝畏三棱",
                    List.of("牙硝"), List.of("三棱")),
            new Rule("十九畏·官桂石脂", LEVEL_WEI, "官桂畏赤石脂",
                    List.of("官桂", "肉桂"), List.of("赤石脂")),
            new Rule("十九畏·人参五灵脂", LEVEL_WEI, "人参畏五灵脂",
                    List.of("人参"), List.of("五灵脂")));

    @Override
    public CompatibilityResultView check(List<String> herbNames) {
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

        // 同一条规则同一对药味只报一次（不同别名命中同对药时去重）
        Set<String> seen = new LinkedHashSet<>();
        List<CompatibilityResultView.Finding> findings = new ArrayList<>();
        for (Rule rule : RULES) {
            for (String aliasA : rule.sideA()) {
                for (String aliasB : rule.sideB()) {
                    for (String herbA : herbs) {
                        if (!contains(herbA, aliasA)) {
                            continue;
                        }
                        for (String herbB : herbs) {
                            if (herbA.equals(herbB) || !contains(herbB, aliasB)) {
                                continue;
                            }
                            String key = rule.group() + "|" + herbA + "|" + herbB;
                            if (seen.add(key)) {
                                findings.add(CompatibilityResultView.Finding.builder()
                                        .level(rule.level())
                                        .rule(rule.group())
                                        .a(herbA)
                                        .b(herbB)
                                        .note(rule.note())
                                        .build());
                            }
                        }
                    }
                }
            }
        }
        findings.sort(Comparator.comparingInt((CompatibilityResultView.Finding f) ->
                        LEVEL_FAN.equals(f.getLevel()) ? 0 : 1)
                .thenComparing(CompatibilityResultView.Finding::getRule)
                .thenComparing(CompatibilityResultView.Finding::getA));

        return CompatibilityResultView.builder()
                .checked(herbs.size())
                .findings(findings)
                .build();
    }

    /** 自由文本药名包含别名即命中（「法半夏」命中「半夏」）；别名按完整词匹配，不做单字模糊 */
    private boolean contains(String herbName, String alias) {
        return !alias.isBlank() && herbName.contains(alias);
    }
}
