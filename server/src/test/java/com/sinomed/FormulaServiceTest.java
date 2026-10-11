package com.sinomed;

import com.sinomed.service.FormulaService;
import com.sinomed.vo.FormulaView;
import com.sinomed.vo.PrescriptionItemView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 方剂库：方名唯一（1–20 字）、拼音码全拼小写（自动转小写）、药味至少 1 味；
 * 更新全量替换药味；keyword 同时匹配方名与拼音码；删除连同药味。
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:sqlite:target/formula-test-${random.uuid}.db"})
@Transactional
class FormulaServiceTest {

    @Autowired
    private FormulaService formulaService;

    private FormulaView view(String name, String pinyin, String... herbs) {
        FormulaView view = FormulaView.builder()
                .name(name).pinyin(pinyin).source("太平惠民和剂局方").indication("演示主治").build();
        view.setHerbs(java.util.Arrays.stream(herbs)
                .map(h -> PrescriptionItemView.builder().herb(h).weight(9.0).build()).toList());
        return view;
    }

    @Test
    void saveAndFindWithHerbsSortedBySort() {
        FormulaView saved = formulaService.findById(
                formulaService.save(view("逍遥散", "xiaoyaosan", "柴胡", "当归", "白芍", "薄荷")).getId());
        assertEquals("逍遥散", saved.getName());
        assertEquals(4, saved.getHerbs().size(), "药味按 sort 存取：" + saved.getHerbs());
        assertEquals("柴胡", saved.getHerbs().get(0).getHerb());
        assertTrue(saved.getCreateTime() != null, "创建时间由审计写入");
    }

    @Test
    void pinyinNormalizedAndSearchMatchesNameAndPinyin() {
        formulaService.save(view("玉屏风散", "YuPingFengSan", "黄芪", "白术", "防风"));
        FormulaView saved = formulaService.findById(
                formulaService.findPage("yuping", PageRequest.of(0, 10)).getContent().get(0).getId());
        assertEquals("yupingfengsan", saved.getPinyin(), "拼音码应存为全拼小写");
        assertEquals(1, formulaService.findPage("屏风", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, formulaService.findPage("yupingfeng", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(0, formulaService.findPage("六味", PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void updateReplacesHerbs() {
        Long id = formulaService.save(view("二陈汤", "erchantang", "半夏", "陈皮", "茯苓", "甘草")).getId();
        FormulaView update = view("二陈汤", "erchantang", "半夏", "陈皮");
        update.setId(id);
        formulaService.update(update);
        assertEquals(2, formulaService.findById(id).getHerbs().size(), "药味应全量替换");
    }

    @Test
    void deleteRemovesFormulaAndHerbs() {
        Long id = formulaService.save(view("平胃散", "pingweisan", "苍术", "厚朴", "陈皮", "甘草")).getId();
        formulaService.deleteById(id);
        try {
            formulaService.findById(id);
            throw new AssertionError("删除后查询应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("方剂不存在"));
        }
    }

    @Test
    void validationGuards() {
        record Case(String name, String pinyin, String expect) {}
        List<Case> bad = List.of(
                new Case("  ", "x", "方剂名不能为空"),
                new Case("字".repeat(21), "x", "方剂名限 20 字内"),
                new Case("新方", "  ", "拼音检索码不能为空"),
                new Case("新方", "Xiao-Yao1", "拼音检索码只能为小写字母"));
        for (Case c : bad) {
            try {
                formulaService.save(view(c.name(), c.pinyin(), "甘草"));
                throw new AssertionError("应报 400：" + c.name());
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains(c.expect()), c.expect() + " ← " + e.getMessage());
            }
        }
        formulaService.save(view("四君子汤", "sijunzitang", "人参", "白术"));
        try {
            formulaService.save(view("四君子汤", "sijunzitaang", "人参"));
            throw new AssertionError("重名应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("方剂名已存在"));
        }
        try {
            formulaService.save(view("新方", "xinfang"));
            throw new AssertionError("无药味应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("方剂至少要有 1 味药"));
        }
    }
}
