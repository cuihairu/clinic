package com.sinomed.service;

import com.sinomed.entity.PrescriptionEntity;
import com.sinomed.vo.PrescriptionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PrescriptionService {
    /**
     * 开方：落 prescriptions 主单 + prescription_items 药味（一个事务）
     */
    PrescriptionEntity save(PrescriptionView view);

    /**
     * 处方详情（含药味）
     */
    PrescriptionView findById(Long id);

    /**
     * 处方分页；customerId 为空查全部
     */
    Page<PrescriptionEntity> findPage(Long customerId, Pageable pageable);

    /**
     * 删除处方（连同药味）
     */
    void deleteById(Long id);

    /**
     * 代煎流转：待煎(1)→可取(2)→已取(3) 顺序推进，其余拒绝
     */
    PrescriptionEntity setDecoctionStatus(Long id, Integer status);
}
