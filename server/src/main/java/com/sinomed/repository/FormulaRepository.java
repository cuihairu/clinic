package com.sinomed.repository;

import com.sinomed.entity.FormulaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FormulaRepository extends JpaRepository<FormulaEntity, Long> {
    Optional<FormulaEntity> findByName(String name);

    /** 方剂检索：方名或拼音检索码模糊匹配，id 倒序（keyword 传空串即全量） */
    @Query("select f from FormulaEntity f where f.name like concat('%', :keyword, '%') "
            + "or f.pinyin like concat('%', :keyword, '%') order by f.id desc")
    Page<FormulaEntity> search(@Param("keyword") String keyword, Pageable pageable);
}
