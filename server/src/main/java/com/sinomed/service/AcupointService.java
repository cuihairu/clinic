package com.sinomed.service;

import com.sinomed.entity.AcupointEntity;
import com.sinomed.vo.AcupointView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AcupointService {
    /**
     * 收录穴位：name（1–10 字，唯一）+ pinyin（全拼小写）+ meridian（归经）必填
     */
    AcupointEntity save(AcupointView view);

    /**
     * 按 id 全量更新，换名撞其他穴位报 400
     */
    AcupointEntity update(AcupointView view);

    /**
     * 穴位详情；不存在报 400
     */
    AcupointView findById(Long id);

    /**
     * 分页检索：keyword 模糊匹配穴名或拼音码（空即全量），id 倒序
     */
    Page<AcupointEntity> findPage(String keyword, Pageable pageable);

    /**
     * 删除穴位（演示环境口径，无留痕）
     */
    void deleteById(Long id);
}
