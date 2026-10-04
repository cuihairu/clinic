package com.sinomed.repository;

import com.sinomed.entity.ReviewCustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCustomerRepository extends JpaRepository<ReviewCustomerEntity,Long> {
}
