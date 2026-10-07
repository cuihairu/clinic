package com.sinomed.repository;

import com.sinomed.entity.AdMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdMaterialRepository extends JpaRepository<AdMaterialEntity, Long> {
    List<AdMaterialEntity> findByEnabledOrderBySortAscIdAsc(Integer enabled);

    List<AdMaterialEntity> findByIdInAndEnabledOrderBySortAscIdAsc(List<Long> ids, Integer enabled);

    Optional<AdMaterialEntity> findTopByOrderByUpdateTimeDesc();
}
