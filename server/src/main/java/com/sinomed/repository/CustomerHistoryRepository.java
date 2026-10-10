package com.sinomed.repository;

import com.sinomed.entity.CustomerHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerHistoryRepository extends JpaRepository<CustomerHistoryEntity, Long> {

    /** 病史记录（新记录在前） */
    List<CustomerHistoryEntity> findByCustomerIdOrderByIdDesc(Long customerId);
}
