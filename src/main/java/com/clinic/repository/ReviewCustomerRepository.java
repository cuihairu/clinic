package com.clinic.repository;

import com.clinic.entity.ReviewCustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCustomerRepository extends JpaRepository<ReviewCustomerEntity,Long> {
}
