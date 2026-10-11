package com.sinomed.repository;

import com.sinomed.entity.HerbStockLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HerbStockLogRepository extends JpaRepository<HerbStockLogEntity, Long>, JpaSpecificationExecutor<HerbStockLogEntity> {
}
