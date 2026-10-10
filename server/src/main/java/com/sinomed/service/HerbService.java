package com.sinomed.service;

import com.sinomed.entity.HerbEntity;
import com.sinomed.vo.HerbView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 药材字典维护：唯一名 + 每克分价，处方计价的比价依据。
 */
public interface HerbService {

    /** 新建药材（名重复报错） */
    HerbEntity create(HerbView view);

    /** 更新药材（名称查重不包含自身） */
    HerbEntity update(HerbView view);

    /** 删除药材（演示环境口径，无引用检查——计价实时查字典，删后相关药味转未比价） */
    void deleteById(Long id);

    /** 字典分页；keyword 非空按名称包含过滤 */
    Page<HerbEntity> findPage(String keyword, Pageable pageable);
}
