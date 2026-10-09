package com.sinomed.repository;

import com.sinomed.entity.AppointmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Date;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity,Long> {

    /** 管理端分页（status 可选过滤由控制器分流到 findAll / 本方法） */
    Page<AppointmentEntity> findByStatus(Integer status, Pageable pageable);

    /** Kiosk 自助约期频控：同顾客同日在给定状态集合内的预约数（status IN (0,1) 限 1 条） */
    long countByCustomerIdAndStartTimeBetweenAndStatusIn(Long customerId, Date start, Date end, Collection<Integer> statusList);

    /** 按时段过滤分页（date 参数落到 [day 00:00, 次日 00:00) 区间） */
    Page<AppointmentEntity> findByStartTimeBetween(Date start, Date end, Pageable pageable);

    /** 时段 + 状态双过滤分页 */
    Page<AppointmentEntity> findByStartTimeBetweenAndStatus(Date start, Date end, Integer status, Pageable pageable);
}
