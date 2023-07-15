package com.clinic.service;

import com.clinic.entity.ReviewCustomerEntity;
import com.clinic.entity.ReviewEntity;
import com.clinic.entity.ReviewStaffEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ReviewService {
    ReviewStaffEntity saveStaffView(ReviewStaffEntity reviewStaffEntity);
    ReviewCustomerEntity saveCustomerView(ReviewCustomerEntity reviewStaffEntity);

    ReviewEntity saveView(ReviewEntity reviewEntity);
    List<ReviewStaffEntity> findStaffViewByDate(Date date);
    Optional<ReviewEntity> findViewByDate(Date date);
    List<ReviewCustomerEntity> findCustomerViewByDate(Date date);

    boolean deleteStaffViewById(Long id);
    boolean deleteCustomerViewById(Long id);
}
