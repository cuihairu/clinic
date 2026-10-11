package com.sinomed.repository;

import com.sinomed.entity.StaffShiftEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffShiftRepository extends JpaRepository<StaffShiftEntity, Long> {

    Optional<StaffShiftEntity> findByStaffIdAndWeekday(Long staffId, Integer weekday);

    List<StaffShiftEntity> findByStaffIdOrderByWeekdayAsc(Long staffId);

    /** 班次分页：员工/星期过滤由 service 按空值分派到派生查询，排序用 PageRequest 携带 */
    Page<StaffShiftEntity> findByStaffId(Long staffId, Pageable pageable);

    Page<StaffShiftEntity> findByWeekday(Integer weekday, Pageable pageable);

    Page<StaffShiftEntity> findByStaffIdAndWeekday(Long staffId, Integer weekday, Pageable pageable);
}
