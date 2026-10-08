package com.sinomed.service;

import com.sinomed.entity.OrderEntity;
import com.sinomed.vo.OrderSummaryView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    /**
     * 管理端分页；status 为空查全部
     * */
    Page<OrderEntity> findPage(Integer status, Pageable pageable);
    /**
     * 状态流转：只允许 0→1、1→2 正向与 0/1→9 取消；非法流转抛 IllegalArgumentException
     * */
    OrderEntity updateStatus(Long id, Integer status);
    /**
     * 按顾客汇总消费：累计消费=已完成（status=2）订单价格合计
     * */
    OrderSummaryView summaryByCustomer(Long customerId);
}
