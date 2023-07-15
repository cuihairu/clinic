package com.clinic.service;

import com.clinic.entity.CustomerEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    /**
     *  根据用户的id查询用户
     * */
    Optional<CustomerEntity> findById(Long id);

    /**
     * 分页查找
     * */
    Page<CustomerEntity> findAllByPage(String name, Integer age, String phone, String startTime, String endTime, Pageable pageable);
    /**
     * 保存用户
     * */
    CustomerEntity save(CustomerEntity userEntity);
    /**
     * 根据用户的姓名查询用户
     * */
    List<CustomerEntity> findByName(String name);

    /**
     * 根据用户的手机号查询用户
     * */
    Optional<CustomerEntity> findByPhone(String phone);

    /**
     * 根据用户的id删除用户
     * */
    void deleteById(Long id);
}
