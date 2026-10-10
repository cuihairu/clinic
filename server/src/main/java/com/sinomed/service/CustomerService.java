package com.sinomed.service;

import com.sinomed.entity.CustomerEntity;
import com.sinomed.entity.CustomerHistoryEntity;
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
     * 顾客病史列表（新记录在前；顾客不存在报错）
     * */
    List<CustomerHistoryEntity> listHistories(Long customerId);

    /**
     * 新增病史（type 0 过敏 / 1 既往，内容非空 ≤200 字）
     * */
    CustomerHistoryEntity addHistory(Long customerId, Integer type, String content);

    /**
     * 删除病史记录（不存在报错；演示环境口径，无留痕）
     * */
    void deleteHistory(Long historyId);

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
