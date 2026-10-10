package com.sinomed.repository;

import com.sinomed.entity.CardUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardUsageRepository extends JpaRepository<CardUsageEntity, Long> {

    /** 核销记录（新记录在前） */
    List<CardUsageEntity> findByCardIdOrderByIdDesc(Long cardId);
}
