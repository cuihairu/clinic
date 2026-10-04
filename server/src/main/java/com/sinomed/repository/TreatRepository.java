package com.sinomed.repository;

import com.sinomed.entity.TreatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TreatRepository extends JpaRepository<TreatEntity,Long>, JpaSpecificationExecutor<TreatEntity> {
}
