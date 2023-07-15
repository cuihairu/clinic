package com.clinic.service.impl;

import com.clinic.entity.OrderEntity;
import com.clinic.repository.OrderRepository;
import com.clinic.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
}
