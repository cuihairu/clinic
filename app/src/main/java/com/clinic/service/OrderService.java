package com.clinic.service;

import com.clinic.entity.OrderEntity;

import java.util.Optional;

public interface OrderService {
    /**
     * 根据订单的id查询订单
     * */
    Optional<OrderEntity> findById(Long id);
    /**
     * 保存订单
     * */
    OrderEntity save(OrderEntity orderEntity);
    /**
     * 根据订单的id删除订单
     * */
    void deleteById(Long id);
}
