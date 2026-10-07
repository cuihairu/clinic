package com.sinomed.repository;

import com.sinomed.entity.AdScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdScheduleRepository extends JpaRepository<AdScheduleEntity, Long> {
    List<AdScheduleEntity> findByScreenIdAndEnabled(Long screenId, Integer enabled);

    Optional<AdScheduleEntity> findTopByOrderByUpdateTimeDesc();
}
