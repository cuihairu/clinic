package com.sinomed.service.impl;

import com.sinomed.vo.CompatibilityResultView;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 配伍审方（CompatibilityService.check）单测：十八反报禁忌、十九畏报慎用、
 * 自由文本别名包含匹配、空白忽略、同对药去重、禁忌排前。
 */
class CompatibilityServiceImplTest {

    private final CompatibilityServiceImpl service = new CompatibilityServiceImpl();

    @Test
    void cleanPrescriptionHasNoFindings() {
        var result = service.check(List.of("黄芪", "当归", "川芎", "甘草梢 "));
        assertEquals(4, result.getChecked());
        assertTrue(result.getFindings().isEmpty());
    }

    @Test
    void eighteenFanWuTouGroupFlaggedAsForbidden() {
        var result = service.check(List.of("附子", "半夏"));
        assertEquals(1, result.getFindings().size());
        var finding = result.getFindings().get(0);
        assertEquals("禁忌", finding.getLevel());
        assertEquals("十八反·乌头组", finding.getRule());
        assertEquals("附子", finding.getA());
        assertEquals("半夏", finding.getB());
        assertTrue(finding.getNote().contains("半蒌贝蔹及攻乌"));
    }

    @Test
    void ganCaoGroupFlagged() {
        var finding = service.check(List.of("炙甘草", "海藻")).getFindings().get(0);
        assertEquals("禁忌", finding.getLevel());
        assertEquals("十八反·甘草组", finding.getRule());
        assertEquals("炙甘草", finding.getA());
    }

    @Test
    void liLuGroupFlagged() {
        var finding = service.check(List.of("藜芦", "丹参")).getFindings().get(0);
        assertEquals("十八反·藜芦组", finding.getRule());
        assertEquals("丹参", finding.getB());
    }

    @Test
    void nineteenWeiDingXiangYuJinFlaggedAsCaution() {
        var finding = service.check(List.of("丁香", "郁金")).getFindings().get(0);
        assertEquals("慎用", finding.getLevel());
        assertEquals("十九畏·丁香郁金", finding.getRule());
    }

    @Test
    void processedHerbNamesMatchByContainment() {
        // 炮制品名按包含命中（「法半夏」命中「半夏」），回显处方原文
        var finding = service.check(List.of("制附子", "法半夏")).getFindings().get(0);
        assertEquals("制附子", finding.getA());
        assertEquals("法半夏", finding.getB());
    }

    @Test
    void singleSidedHerbAloneHasNoFinding() {
        assertTrue(service.check(List.of("半夏")).getFindings().isEmpty());
        assertTrue(service.check(List.of("川乌", "草乌")).getFindings().isEmpty());
    }

    @Test
    void sameRuleSamePairDedupedAcrossAliases() {
        // 「川乌头」同时命中别名「乌头」与「川乌」，同一对药只报一次
        var findings = service.check(List.of("川乌头", "半夏")).getFindings();
        assertEquals(1, findings.size());
    }

    @Test
    void distinctHerbPairsBothReported() {
        var findings = service.check(List.of("附子", "川乌头", "半夏")).getFindings();
        assertEquals(2, findings.size());
    }

    @Test
    void forbiddenSortedBeforeCaution() {
        var result = service.check(Arrays.asList("丁香", "郁金", "附子", "半夏"));
        assertEquals(2, result.getFindings().size());
        assertEquals("禁忌", result.getFindings().get(0).getLevel());
        assertEquals("慎用", result.getFindings().get(1).getLevel());
    }

    @Test
    void blankNamesIgnoredAndAllBlankRejected() {
        var result = service.check(Arrays.asList(" 附子 ", "", null, "半夏"));
        assertEquals(2, result.getChecked());
        assertEquals(1, result.getFindings().size());
        assertThrows(IllegalArgumentException.class, () -> service.check(Arrays.asList(" ", null)));
        assertThrows(IllegalArgumentException.class, () -> service.check(null));
        var e = assertThrows(IllegalArgumentException.class, () -> service.check(List.of("  ")));
        assertEquals("药材名单不能为空", e.getMessage());
    }
}
