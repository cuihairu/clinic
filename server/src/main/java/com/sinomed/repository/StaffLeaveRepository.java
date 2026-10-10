package com.sinomed.repository;

import com.sinomed.entity.StaffLeaveEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffLeaveRepository extends JpaRepository<StaffLeaveEntity, Long> {

    /** 按员工查请假记录（id 倒序） */
    Page<StaffLeaveEntity> findByStaffId(Long staffId, Pageable pageable);
}
