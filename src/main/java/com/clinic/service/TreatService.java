package com.clinic.service;

import com.clinic.entity.TreatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TreatService {
    /**
     * 根据治疗的id查询治疗
     * */
    Optional<TreatEntity> findById(Long id);
    List<TreatEntity>findByCustomerId(Long customerId);

    Page<TreatEntity> findByPage(TreatEntity treatEntity, Pageable pageable);
    Page<TreatEntity> findAllByPage(List<Long> customerIds, String startTime, String endTime, Pageable pageable);
    /**
     * 保存治疗
     * */
    TreatEntity save(TreatEntity treatEntity);
    /**
     * 根据治疗的id删除治疗
     * */
    void deleteById(Long id);
    /**
     * 根据治疗的id查询治疗
     * */
    List<TreatEntity> findAll();
}
