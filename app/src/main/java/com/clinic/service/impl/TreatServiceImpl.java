package com.clinic.service.impl;

import com.clinic.entity.CustomerEntity;
import com.clinic.entity.TreatEntity;
import com.clinic.repository.TreatRepository;
import com.clinic.service.TreatService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TreatServiceImpl implements TreatService {
    private final TreatRepository treatRepository;
    public TreatServiceImpl(TreatRepository treatRepository){
        this.treatRepository = treatRepository;
    }
    /**
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        treatRepository.deleteById(id);
    }

    /**
     * @param id
     * @return
     */
    @Override
    @Cacheable("TreatEntity")
    public Optional<TreatEntity> findById(Long id) {
        return treatRepository.findById(id);
    }

    @Override
    public List<TreatEntity> findByCustomerId(Long customerId) {
        TreatEntity treatEntity = new TreatEntity();
        treatEntity.setCustomerId(customerId);
        return treatRepository.findAll(Example.of(treatEntity));
    }

    /**
     * @param
     * @return
     */
    @Override
    public Page<TreatEntity> findByPage(TreatEntity treatEntity, Pageable pageable) {
        return treatRepository.findAll(Example.of(treatEntity),pageable);
    }

    @Override
    public Page<TreatEntity> findAllByPage(List<Long> customerIds, String startTime, String endTime, Pageable pageable){
        Specification<TreatEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();
            if (startTime != null && !startTime.trim().isEmpty()) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime").as(String.class), startTime));
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime").as(String.class), endTime));
            }

            if (customerIds != null && !customerIds.isEmpty()){
                CriteriaBuilder.In<Long> inClause = criteriaBuilder.in(root.get("customerId"));
                for (Long cid : customerIds){
                    inClause.value(cid);
                }
                predicates.add(inClause);
            }
            Predicate[] array = new Predicate[predicates.size()];
            return criteriaBuilder.and(predicates.toArray(array));
        };


        return treatRepository.findAll(specification,pageable);
    }

    /**
     * 保存治疗
     *
     * @param treatEntity
     */
    @Override
    public TreatEntity save(TreatEntity treatEntity) {
        // 时间不更新
        if (treatEntity.getId() != null){
            Optional<TreatEntity> byId = treatRepository.findById(treatEntity.getId());
            if (!byId.isEmpty()){
                treatEntity.setCreateTime(byId.get().getCreateTime());
                treatEntity.setUpdateTime(byId.get().getUpdateTime());
            }
        }
        return treatRepository.save(treatEntity);
    }

    /**
     * @return
     */
    @Override
    public List<TreatEntity> findAll() {
        return treatRepository.findAll();
    }
}
