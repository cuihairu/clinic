package com.sinomed;

import com.sinomed.service.AcupointService;
import com.sinomed.vo.AcupointView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 穴位字典：穴名唯一（1–10 字）、拼音码全拼小写（自动转小写）、归经必填；
 * keyword 同时匹配穴名与拼音码；更新换名放行自身、撞他人报 400；删除后查报 400。
 */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:sqlite:target/acupoint-test-${random.uuid}.db"})
@Transactional
class AcupointServiceTest {

    @Autowired
    private AcupointService acupointService;

    private AcupointView view(String name, String pinyin, String meridian) {
        return AcupointView.builder()
                .name(name).pinyin(pinyin).meridian(meridian)
                .location("演示定位").indication("演示主治").build();
    }

    @Test
    void saveAndFindWithAuditTime() {
        AcupointView saved = acupointService.findById(
                acupointService.save(view("足三里", "ZuSanLi", "足阳明胃经")).getId());
        assertEquals("足三里", saved.getName());
        assertEquals("zusanli", saved.getPinyin(), "拼音码应存为全拼小写");
        assertEquals("足阳明胃经", saved.getMeridian());
        assertTrue(saved.getCreateTime() != null, "创建时间由审计写入");
    }

    @Test
    void pinyinNormalizedAndSearchMatchesNameAndPinyin() {
        acupointService.save(view("三阴交", "SanyinJiao", "足太阴脾经"));
        AcupointView saved = acupointService.findById(
                acupointService.findPage("sanyin", PageRequest.of(0, 10)).getContent().get(0).getId());
        assertEquals("sanyinjiao", saved.getPinyin());
        assertEquals(1, acupointService.findPage("三阴", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, acupointService.findPage("sanyin", PageRequest.of(0, 10)).getTotalElements());
        assertEquals(0, acupointService.findPage("太溪", PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void updateKeepsSelfNameAndClearsLocation() {
        Long id = acupointService.save(view("阳陵泉", "yanglingquan", "足少阳胆经")).getId();
        AcupointView update = AcupointView.builder().id(id).name("阳陵泉").pinyin("yanglingquan")
                .meridian("足少阳胆经").location("").build();
        acupointService.update(update);
        AcupointView saved = acupointService.findById(id);
        assertEquals("阳陵泉", saved.getName(), "换名放行自身");
        assertEquals("足少阳胆经", saved.getMeridian());
        assertNull(saved.getLocation(), "空白定位应清空");
        assertNull(saved.getIndication());
    }

    @Test
    void deleteRemovesPoint() {
        Long id = acupointService.save(view("委中", "weizhong", "足太阳膀胱经")).getId();
        acupointService.deleteById(id);
        try {
            acupointService.findById(id);
            throw new AssertionError("删除后查询应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("穴位不存在"));
        }
    }

    @Test
    void validationGuards() {
        record Case(String name, String pinyin, String meridian, String expect) {}
        List<Case> bad = List.of(
                new Case("  ", "x", "足阳明胃经", "穴位名不能为空"),
                new Case("字".repeat(11), "x", "足阳明胃经", "穴位名限 10 字内"),
                new Case("新穴", "  ", "足阳明胃经", "拼音检索码不能为空"),
                new Case("新穴", "Zu San1", "足阳明胃经", "拼音检索码只能为小写字母"),
                new Case("新穴", "xinxue", "  ", "归经不能为空"),
                new Case("新穴", "xinxue", "字".repeat(21), "归经限 20 字内"));
        for (Case c : bad) {
            try {
                acupointService.save(view(c.name(), c.pinyin(), c.meridian()));
                throw new AssertionError("应报 400：" + c.name());
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains(c.expect()), c.expect() + " ← " + e.getMessage());
            }
        }
        acupointService.save(view("四神聪", "sishencong", "经外奇穴"));
        try {
            acupointService.save(view("四神聪", "sishenconng", "经外奇穴"));
            throw new AssertionError("重名应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("穴位名已存在"));
        }
        AcupointView update = AcupointView.builder().id(99L).name("委中").pinyin("weizhong")
                .meridian("足太阳膀胱经").build();
        try {
            acupointService.update(update);
            throw new AssertionError("未知 id 应报 400");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("穴位不存在"));
        }
    }
}
