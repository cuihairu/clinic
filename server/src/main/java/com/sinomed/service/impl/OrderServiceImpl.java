package com.sinomed.service.impl;

import com.sinomed.entity.OrderEntity;
import com.sinomed.repository.OrderRepository;
import com.sinomed.service.OrderService;
import com.sinomed.vo.OrderSummaryView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    /**
     * 根据订单的id查询订单
     *
     * @param id
     */
    @Override
    @Cacheable("OrderEntity")
    public Optional<OrderEntity> findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * 保存订单
     *
     * @param orderEntity
     */
    @Override
    public OrderEntity save(OrderEntity orderEntity) {
        Long id = orderEntity.getId();
        if (id != null){
            Optional<OrderEntity> byId = orderRepository.findById(id);
            if (!byId.isEmpty()){
                orderEntity.setCreateTime(byId.get().getCreateTime());
                orderEntity.setUpdateTime(byId.get().getUpdateTime());
            }
        }
        return orderRepository.save(orderEntity);
    }

    /**
     * 根据订单的id删除订单
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    /**
     * 管理端分页；status 为空查全部
     */
    @Override
    public Page<OrderEntity> findPage(Integer status, Pageable pageable) {
        if (status == null) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findByStatus(status, pageable);
    }

    /**
     * 状态流转：只允许 0→1、1→2 正向与 0/1→9 取消
     */
    @Override
    public OrderEntity updateStatus(Long id, Integer status) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在：" + id));
        Integer current = order.getStatus();
        boolean allowed = (current == null || current == 0) && (status == 1 || status == 9)
                || current != null && current == 1 && (status == 2 || status == 9);
        if (!allowed) {
            throw new IllegalArgumentException("订单状态不允许从 " + current + " 流转到 " + status);
        }
        order.setStatus(status);
        return orderRepository.save(order);
    }

    /**
     * 按顾客汇总消费：只统计已完成（status=2）的订单，价格空视为 0
     */
    @Override
    public OrderSummaryView summaryByCustomer(Long customerId) {
        List<OrderEntity> done = orderRepository.findByUserIdAndStatus(customerId, 2);
        long amount = done.stream().mapToLong(order -> order.getPrice() == null ? 0L : order.getPrice()).sum();
        return OrderSummaryView.builder()
                .orders((long) done.size())
                .amount(amount)
                .build();
    }
}
