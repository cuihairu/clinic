package com.clinic.service;

import com.clinic.entity.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ItemService {
    /**
     * 根据项目的id查询项目
     * */
    Optional<ItemEntity> findById(Long id);

    Page<ItemEntity> finAll(ItemEntity itemEntity, Pageable pageable);

    Optional<ItemEntity> findByName(String name);
    /**
     * 保存项目
     * */
    ItemEntity save(ItemEntity itemEntity);
    /**
     * 根据项目的id删除项目
     * */
    void deleteById(Long id);
}
