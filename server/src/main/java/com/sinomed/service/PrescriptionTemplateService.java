package com.sinomed.service;

import com.sinomed.entity.PrescriptionTemplateEntity;
import com.sinomed.vo.PrescriptionTemplateView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PrescriptionTemplateService {
    /**
     * 建模板：落 prescription_templates 主单 + prescription_template_items 药味（一个事务）；模板名唯一
     */
    PrescriptionTemplateEntity save(PrescriptionTemplateView view);

    /**
     * 按模板名查重（唯一性校验用）
     */
    boolean existsByName(String name);

    /**
     * 更新模板（药味全量替换，一个事务）；id 必填
     */
    PrescriptionTemplateEntity update(PrescriptionTemplateView view);

    /**
     * 模板详情（含药味）
     */
    PrescriptionTemplateView findById(Long id);

    /**
     * 模板分页；name 模糊过滤可空
     */
    Page<PrescriptionTemplateEntity> findPage(String name, Pageable pageable);

    /**
     * 上架模板（enabled=1，id 升序），开方页「套用模板」取数入口；附药味
     */
    List<PrescriptionTemplateView> findEnabledWithHerbs();

    /**
     * 删除模板（连同药味）
     */
    void deleteById(Long id);
}
