package com.sinomed;

import com.sinomed.entity.HerbEntity;
import com.sinomed.repository.HerbRepository;
import com.sinomed.service.HerbService;
import com.sinomed.vo.HerbView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 药材字典（HerbService）回归：唯一名收录、查重（新建与改名）、价格校验、
 * 删除与关键字分页。库路径随机文件，避免污染。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/herb-service-test-${random.uuid}.db"
})
class HerbServiceTest {

    @Autowired
    private HerbService herbService;

    @Autowired
    private HerbRepository herbRepository;

    private static Long ganCaoId;

    @BeforeEach
    void seed() {
        if (ganCaoId == null) {
            ganCaoId = herbService.create(view("甘草", 2)).getId();
        }
    }

    private HerbView view(String name, Integer price) {
        return HerbView.builder().name(name).price(price).build();
    }

    @Test
    void createTrimsNameAndSavesPrice() {
        var saved = herbService.create(view(" 黄芪 ", 6));
        assertTrue(saved.getId() > 0);
        assertEquals("黄芪", herbRepository.findById(saved.getId()).orElseThrow().getName());
        assertEquals(6, herbRepository.findById(saved.getId()).orElseThrow().getPrice());
    }

    @Test
    void createDuplicateNameRejected() {
        var e = assertThrows(IllegalArgumentException.class, () -> herbService.create(view("甘草", 3)));
        assertEquals("药材已收录：甘草", e.getMessage());
    }

    @Test
    void updateChangesPriceAndRejectsRenameToExisting() {
        herbService.update(HerbView.builder().id(ganCaoId).name("甘草").price(3).build());
        assertEquals(3, herbRepository.findById(ganCaoId).orElseThrow().getPrice());

        var other = herbService.create(view("白芍", 5));
        var e = assertThrows(IllegalArgumentException.class,
                () -> herbService.update(HerbView.builder().id(other.getId()).name("甘草").price(5).build()));
        assertEquals("药材已收录：甘草", e.getMessage());
    }

    @Test
    void priceMustBePositive() {
        var e = assertThrows(IllegalArgumentException.class, () -> herbService.create(view("大枣", 0)));
        assertEquals("每克价格必须大于 0", e.getMessage());
    }

    @Test
    void blankNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> herbService.create(view("  ", 5)));
    }

    @Test
    void deleteRemovesEntry() {
        var temp = herbService.create(view("薄荷", 3));
        herbService.deleteById(temp.getId());
        assertFalse(herbRepository.existsById(temp.getId()));
        assertThrows(IllegalArgumentException.class, () -> herbService.deleteById(temp.getId()));
    }

    @Test
    void pageFiltersByKeywordContains() {
        herbService.create(view("炙甘草", 2));
        // 同库其他用例会建黄芪/白芍等，关键字断言只看本用例可确定的数据
        var ganCao = herbService.findPage("甘草", PageRequest.of(0, 10));
        assertEquals(2, ganCao.getTotalElements());
        var zhi = herbService.findPage("炙甘", PageRequest.of(0, 10));
        assertEquals(1, zhi.getTotalElements());
        assertEquals("炙甘草", zhi.getContent().get(0).getName());
    }
}
