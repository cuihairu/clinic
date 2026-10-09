package com.sinomed.repository;

import com.sinomed.entity.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {
    Optional<SettlementEntity> findByOrderId(Long orderId);

    List<SettlementEntity> findByOrderIdIn(Collection<Long> orderIds);
}
