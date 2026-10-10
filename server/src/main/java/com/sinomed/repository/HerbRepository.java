package com.sinomed.repository;

import com.sinomed.entity.HerbEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HerbRepository extends JpaRepository<HerbEntity, Long> {

    HerbEntity findByName(String name);

    List<HerbEntity> findByNameIn(Collection<String> names);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Page<HerbEntity> findByNameContaining(String keyword, Pageable pageable);
}
