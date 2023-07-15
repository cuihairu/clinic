package com.clinic.repository;

import com.clinic.entity.SignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SignRepository extends JpaRepository<SignEntity,Long>, JpaSpecificationExecutor<SignEntity> {
}
