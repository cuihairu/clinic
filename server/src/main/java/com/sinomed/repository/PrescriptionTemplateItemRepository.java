package com.sinomed.repository;

import com.sinomed.entity.PrescriptionTemplateItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionTemplateItemRepository extends JpaRepository<PrescriptionTemplateItemEntity, Long> {
    List<PrescriptionTemplateItemEntity> findByTemplateIdOrderBySortAsc(Long templateId);

    List<PrescriptionTemplateItemEntity> findByTemplateIdInOrderBySortAsc(List<Long> templateIds);

    void deleteByTemplateId(Long templateId);
}
