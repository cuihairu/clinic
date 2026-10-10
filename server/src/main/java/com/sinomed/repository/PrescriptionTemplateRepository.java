package com.sinomed.repository;

import com.sinomed.entity.PrescriptionTemplateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrescriptionTemplateRepository extends JpaRepository<PrescriptionTemplateEntity, Long> {
    Optional<PrescriptionTemplateEntity> findByName(String name);

    Page<PrescriptionTemplateEntity> findByNameContainingOrderByIdDesc(String name, Pageable pageable);

    List<PrescriptionTemplateEntity> findByEnabledOrderByIdAsc(Integer enabled);
}
