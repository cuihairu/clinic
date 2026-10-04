package com.clinic.service.impl;

import com.clinic.entity.CustomerEntity;
import com.clinic.repository.CustomerRepository;
import com.clinic.service.CustomerService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Page<CustomerEntity> findAllByPage(String name,Integer age,String phone, String startTime,String endTime, Pageable pageable){
        if ((name == null || name.isEmpty()) && (age == null || age == 0) &&(phone==null || phone.isEmpty()) && (startTime==null || startTime.isEmpty()) &&(endTime==null || endTime.isEmpty()) ){
            return customerRepository.findAll(pageable);
        }
        Specification<CustomerEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();
            if (name != null && !name.isEmpty()){
                predicates.add(criteriaBuilder.equal(root.get("name").as(String.class),name));
            }
            if (age != null && age > 0) {
                predicates.add(criteriaBuilder.equal(root.get("age").as(Integer.class),age));
            }
            if (phone != null && !phone.isEmpty()){
                predicates.add(criteriaBuilder.equal(root.get("phone").as(String.class),phone));
            }
            if (startTime != null && !startTime.trim().isEmpty()) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime").as(String.class), startTime));
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime").as(String.class), endTime));
            }
            Predicate[] array = new Predicate[predicates.size()];
            return criteriaBuilder.and(predicates.toArray(array));
        };
        return customerRepository.findAll(specification,pageable);
    }
    /**
     * @param id
     * @return
     */
    @Override
    @Cacheable("customerEntity")
    public Optional<CustomerEntity> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public CustomerEntity save(CustomerEntity customerEntity){
        Long id = customerEntity.getId();
        if (id != null){
            Optional<CustomerEntity> byId = customerRepository.findById(id);
            if (!byId.isEmpty()){
                customerEntity.setCreateTime(byId.get().getCreateTime());
                customerEntity.setUpdateTime(byId.get().getUpdateTime());
            }
        }
        return customerRepository.save(customerEntity);
    }

    @Override
    public List<CustomerEntity> findByName(String name){
        CustomerEntity userEntity = new CustomerEntity();
        userEntity.setName(name);
        return customerRepository.findAll(Example.of(userEntity));
    }

    @Override
    public Optional<CustomerEntity> findByPhone(String phone){
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setPhone(phone);
        return customerRepository.findOne(Example.of(customerEntity));
    }

    /**
     * 根据用户的id删除用户
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }
}
