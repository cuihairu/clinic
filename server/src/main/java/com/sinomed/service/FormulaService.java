package com.sinomed.service;

import com.sinomed.entity.FormulaEntity;
import com.sinomed.vo.FormulaView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FormulaService {
    /**
     * 建方剂：name（1–20 字，唯一）+ pinyin（全拼小写）+ herbs（至少 1 味）必填
     */
    FormulaEntity save(FormulaView view);

    /**
     * 按 id 全量更新：药味全量替换（先删后插），换名撞其他方剂报 400
     */
    FormulaEntity update(FormulaView view);

    /**
     * 方剂详情（含药味）；不存在报 400
     */
    FormulaView findById(Long id);

    /**
     * 分页检索：keyword 模糊匹配方名或拼音码（空即全量），id 倒序
     */
    Page<FormulaEntity> findPage(String keyword, Pageable pageable);

    /**
     * 删除方剂（连同药味；演示环境口径，无留痕）
     */
    void deleteById(Long id);
}
