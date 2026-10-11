package com.sinomed.repository;

import com.sinomed.entity.FormulaItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormulaItemRepository extends JpaRepository<FormulaItemEntity, Long> {
    List<FormulaItemEntity> findByFormulaIdOrderBySortAsc(Long formulaId);

    List<FormulaItemEntity> findByFormulaIdInOrderBySortAsc(List<Long> formulaIds);

    void deleteByFormulaId(Long formulaId);
}
