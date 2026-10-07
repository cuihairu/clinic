package com.sinomed.repository;

import com.sinomed.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity,Long>, JpaSpecificationExecutor<CustomerEntity> {

    /** Kiosk 下单按手机号找顾客（唯一键），查无即建档 */
    Optional<CustomerEntity> findByPhone(String phone);
}
