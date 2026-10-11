package com.sinomed.service.impl;

import com.sinomed.entity.FormulaEntity;
import com.sinomed.entity.FormulaItemEntity;
import com.sinomed.repository.FormulaItemRepository;
import com.sinomed.repository.FormulaRepository;
import com.sinomed.service.FormulaService;
import com.sinomed.vo.FormulaView;
import com.sinomed.vo.PrescriptionItemView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 方剂库实现：方名唯一（1–20 字）；拼音检索码为全拼小写字母；药味至少 1 味、剂量 > 0。
 * 更新走「删旧药味 + 插新药味」全量替换。方剂为参考资料，开方带出后随单自由改。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FormulaServiceImpl implements FormulaService {

    private final FormulaRepository formulaRepository;
    private final FormulaItemRepository formulaItemRepository;

    @Override
    @Transactional
    public FormulaEntity save(FormulaView view) {
        String name = validName(view, null);
        String pinyin = validPinyin(view == null ? null : view.getPinyin());
        List<PrescriptionItemView> herbs = validHerbs(view.getHerbs());

        FormulaEntity formula = new FormulaEntity();
        formula.setName(name);
        formula.setPinyin(pinyin);
        formula.setSource(trimOrNull(view.getSource()));
        formula.setIndication(trimOrNull(view.getIndication()));
        formula = formulaRepository.save(formula);
        saveItems(formula.getId(), herbs);
        return formula;
    }

    @Override
    @Transactional
    public FormulaEntity update(FormulaView view) {
        if (view == null || view.getId() == null) {
            throw new IllegalArgumentException("方剂id不能为空");
        }
        FormulaEntity formula = formulaRepository.findById(view.getId())
                .orElseThrow(() -> new IllegalArgumentException("方剂不存在：" + view.getId()));
        String name = validName(view, formula.getId());
        String pinyin = validPinyin(view.getPinyin());
        List<PrescriptionItemView> herbs = validHerbs(view.getHerbs());

        formula.setName(name);
        formula.setPinyin(pinyin);
        formula.setSource(trimOrNull(view.getSource()));
        formula.setIndication(trimOrNull(view.getIndication()));
        formula = formulaRepository.save(formula);
        // 药味全量替换（先删后插）
        formulaItemRepository.deleteByFormulaId(formula.getId());
        saveItems(formula.getId(), herbs);
        return formula;
    }

    @Override
    public FormulaView findById(Long id) {
        FormulaEntity formula = formulaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("方剂不存在：" + id));
        FormulaView view = FormulaView.FromEntity(formula);
        view.setHerbs(formulaItemRepository.findByFormulaIdOrderBySortAsc(id).stream()
                .map(PrescriptionItemView::FromFormulaItemEntity).toList());
        return view;
    }

    @Override
    public Page<FormulaEntity> findPage(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return formulaRepository.search(kw, pageable);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        formulaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("方剂不存在：" + id));
        formulaItemRepository.deleteByFormulaId(id);
        formulaRepository.deleteById(id);
    }

    /** 方名校验：非空、限 20 字、唯一（更新时放过自身） */
    private String validName(FormulaView view, Long selfId) {
        if (view == null || view.getName() == null || view.getName().isBlank()) {
            throw new IllegalArgumentException("方剂名不能为空");
        }
        String name = view.getName().trim();
        if (name.length() > 20) {
            throw new IllegalArgumentException("方剂名限 20 字内");
        }
        formulaRepository.findByName(name)
                .filter(other -> !other.getId().equals(selfId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("方剂名已存在：" + name);
                });
        return name;
    }

    /** 拼音检索码校验：非空、全拼小写字母（检索按 contains 匹配） */
    private String validPinyin(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("拼音检索码不能为空");
        }
        String pinyin = raw.trim().toLowerCase();
        if (!pinyin.matches("[a-z]+")) {
            throw new IllegalArgumentException("拼音检索码只能为小写字母：" + raw);
        }
        return pinyin;
    }

    private String trimOrNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }

    /** 药味校验：至少 1 味、药名非空、剂量 > 0 */
    private List<PrescriptionItemView> validHerbs(List<PrescriptionItemView> herbs) {
        if (herbs == null || herbs.isEmpty()) {
            throw new IllegalArgumentException("方剂至少要有 1 味药");
        }
        for (int i = 0; i < herbs.size(); i++) {
            PrescriptionItemView herb = herbs.get(i);
            if (herb == null || herb.getHerb() == null || herb.getHerb().isBlank()) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 味药名不能为空");
            }
            if (herb.getWeight() == null || herb.getWeight() <= 0) {
                throw new IllegalArgumentException("药材「" + herb.getHerb() + "」剂量必须大于 0");
            }
        }
        return herbs;
    }

    /** 药味落库：顺序按列表序号，覆盖同名的前序 sort */
    private void saveItems(Long formulaId, List<PrescriptionItemView> herbs) {
        for (int i = 0; i < herbs.size(); i++) {
            PrescriptionItemView herb = herbs.get(i);
            FormulaItemEntity item = new FormulaItemEntity();
            item.setFormulaId(formulaId);
            item.setHerb(herb.getHerb().trim());
            item.setWeight(herb.getWeight());
            item.setSpecial(herb.getSpecial());
            item.setSort(i);
            formulaItemRepository.save(item);
        }
    }
}
