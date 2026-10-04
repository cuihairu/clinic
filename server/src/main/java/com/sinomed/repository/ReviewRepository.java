package com.sinomed.repository;

import com.sinomed.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity,Long> {
}
