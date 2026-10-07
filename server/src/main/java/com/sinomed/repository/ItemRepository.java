package com.sinomed.repository;

import com.sinomed.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity,Long>{

    /** Kiosk 浏览：上架项按 sort、name 升序 */
    List<ItemEntity> findByEnabledOrderBySortAscNameAsc(Integer enabled);
}
