package com.sinomed.repository;

import com.sinomed.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity,Long> {

    /** 管理端订单分页（status 可选过滤由控制器分流到 findAll / 本方法） */
    Page<OrderEntity> findByStatus(Integer status, Pageable pageable);

    /** 按顾客+状态查订单（顾客档案消费汇总用） */
    List<OrderEntity> findByUserIdAndStatus(Long userId, Integer status);
}
