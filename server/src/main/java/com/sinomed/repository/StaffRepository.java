package com.sinomed.repository;

import com.sinomed.entity.StaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StaffRepository extends JpaRepository<StaffEntity,Long>, JpaSpecificationExecutor<StaffEntity> {
}
