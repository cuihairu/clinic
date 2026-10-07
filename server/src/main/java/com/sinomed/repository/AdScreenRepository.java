package com.sinomed.repository;

import com.sinomed.entity.AdScreenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdScreenRepository extends JpaRepository<AdScreenEntity, Long> {
    Optional<AdScreenEntity> findByCode(String code);

    Optional<AdScreenEntity> findByCodeAndEnabled(String code, Integer enabled);
}
