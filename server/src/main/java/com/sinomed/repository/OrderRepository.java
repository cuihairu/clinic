package com.sinomed.repository;

import com.sinomed.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity,Long> {

    /** 管理端订单分页（status 可选过滤由控制器分流到 findAll / 本方法） */
    Page<OrderEntity> findByStatus(Integer status, Pageable pageable);
}
