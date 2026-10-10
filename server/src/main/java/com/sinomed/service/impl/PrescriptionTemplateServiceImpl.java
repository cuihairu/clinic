package com.sinomed.service.impl;

import com.sinomed.entity.PrescriptionTemplateEntity;
import com.sinomed.entity.PrescriptionTemplateItemEntity;
import com.sinomed.repository.PrescriptionTemplateItemRepository;
import com.sinomed.repository.PrescriptionTemplateRepository;
import com.sinomed.service.PrescriptionTemplateService;
import com.sinomed.vo.PrescriptionItemView;
import com.sinomed.vo.PrescriptionTemplateView;
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
 * 病症处方模板实现：模板名唯一（1–20 字）；药味至少 1 味、剂量 > 0、剂数 ≥ 1；
 * 创建/更新时间由审计维护。更新走「删旧药味 + 插新药味」全量替换，与开方单互不影响。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PrescriptionTemplateServiceImpl implements PrescriptionTemplateService {

    private final PrescriptionTemplateRepository templateRepository;
    private final PrescriptionTemplateItemRepository templateItemRepository;

    @Override
    @Transactional
    public PrescriptionTemplateEntity save(PrescriptionTemplateView view) {
        if (view == null || view.getName() == null || view.getName().isBlank()) {
            throw new IllegalArgumentException("病症名不能为空");
        }
        if (view.getName().trim().length() > 20) {
            throw new IllegalArgumentException("病症名限 20 字内");
        }
        if (templateRepository.findByName(view.getName().trim()).isPresent()) {
            throw new IllegalArgumentException("模板名已存在：" + view.getName().trim());
        }
        int doses = view.getDoses() == null ? 7 : view.getDoses();
        if (doses < 1) {
            throw new IllegalArgumentException("剂数必须 ≥ 1");
        }
        List<PrescriptionItemView> herbs = validHerbs(view.getHerbs());

        PrescriptionTemplateEntity template = new PrescriptionTemplateEntity();
        template.setName(view.getName().trim());
        template.setDoses(doses);
        template.setDecoction(view.getDecoction() == null ? 0 : view.getDecoction());
        template.setUsage(view.getUsage());
        template.setRemark(view.getRemark());
        template.setEnabled(view.getEnabled() == null ? 1 : view.getEnabled());
        template = templateRepository.save(template);
        saveItems(template.getId(), herbs);
        return template;
    }

    @Override
    public boolean existsByName(String name) {
        return name != null && templateRepository.findByName(name.trim()).isPresent();
    }

    @Override
    @Transactional
    public PrescriptionTemplateEntity update(PrescriptionTemplateView view) {
        if (view == null || view.getId() == null) {
            throw new IllegalArgumentException("模板id不能为空");
        }
        if (view.getName() == null || view.getName().isBlank()) {
            throw new IllegalArgumentException("病症名不能为空");
        }
        PrescriptionTemplateEntity template = templateRepository.findById(view.getId())
                .orElseThrow(() -> new IllegalArgumentException("模板不存在：" + view.getId()));
        // 唯一性：换名撞别的模板才拒
        PrescriptionTemplateEntity current = template;
        templateRepository.findByName(view.getName().trim())
                .filter(other -> !other.getId().equals(current.getId()))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("模板名已存在：" + view.getName().trim());
                });
        int doses = view.getDoses() == null ? 7 : view.getDoses();
        if (doses < 1) {
            throw new IllegalArgumentException("剂数必须 ≥ 1");
        }
        List<PrescriptionItemView> herbs = validHerbs(view.getHerbs());

        template.setName(view.getName().trim());
        template.setDoses(doses);
        template.setDecoction(view.getDecoction() == null ? 0 : view.getDecoction());
        template.setUsage(view.getUsage());
        template.setRemark(view.getRemark());
        template.setEnabled(view.getEnabled() == null ? 1 : view.getEnabled());
        template = templateRepository.save(template);
        // 药味全量替换（先删后插）
        templateItemRepository.deleteByTemplateId(template.getId());
        saveItems(template.getId(), herbs);
        return template;
    }

    @Override
    public PrescriptionTemplateView findById(Long id) {
        PrescriptionTemplateEntity template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在：" + id));
        PrescriptionTemplateView view = PrescriptionTemplateView.FromTemplateEntity(template);
        view.setHerbs(templateItemRepository.findByTemplateIdOrderBySortAsc(id).stream()
                .map(PrescriptionItemView::FromTemplateItemEntity).toList());
        return view;
    }

    @Override
    public Page<PrescriptionTemplateEntity> findPage(String name, Pageable pageable) {
        if (name != null && !name.isBlank()) {
            return templateRepository.findByNameContainingOrderByIdDesc(name.trim(), pageable);
        }
        return templateRepository.findAll(pageable);
    }

    @Override
    public List<PrescriptionTemplateView> findEnabledWithHerbs() {
        List<PrescriptionTemplateEntity> enabled = templateRepository.findByEnabledOrderByIdAsc(1);
        Map<Long, List<PrescriptionItemView>> herbs = templateItemRepository
                .findByTemplateIdInOrderBySortAsc(enabled.stream().map(PrescriptionTemplateEntity::getId).toList())
                .stream().collect(Collectors.groupingBy(PrescriptionTemplateItemEntity::getTemplateId,
                        Collectors.mapping(PrescriptionItemView::FromTemplateItemEntity, Collectors.toList())));
        return enabled.stream().map(t -> {
            PrescriptionTemplateView view = PrescriptionTemplateView.FromTemplateEntity(t);
            view.setHerbs(herbs.get(t.getId()));
            return view;
        }).toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在：" + id));
        templateItemRepository.deleteByTemplateId(id);
        templateRepository.deleteById(id);
    }

    /** 药味校验：至少 1 味、药名非空、剂量 > 0 */
    private List<PrescriptionItemView> validHerbs(List<PrescriptionItemView> herbs) {
        if (herbs == null || herbs.isEmpty()) {
            throw new IllegalArgumentException("模板至少要有 1 味药");
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
    private void saveItems(Long templateId, List<PrescriptionItemView> herbs) {
        for (int i = 0; i < herbs.size(); i++) {
            PrescriptionItemView herb = herbs.get(i);
            PrescriptionTemplateItemEntity item = new PrescriptionTemplateItemEntity();
            item.setTemplateId(templateId);
            item.setHerb(herb.getHerb().trim());
            item.setWeight(herb.getWeight());
            item.setSpecial(herb.getSpecial());
            item.setSort(i);
            templateItemRepository.save(item);
        }
    }
}
