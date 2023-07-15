package com.clinic.service;

import com.clinic.entity.CustomerEntity;
import com.clinic.entity.SignEntity;
import com.clinic.entity.StaffEntity;
import com.clinic.vo.TimesheetView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Optional;

public interface StaffService {

    /**
     * 根据员工的id查询员工
     * */
    Optional<StaffEntity> findById(Long id);
    Page<StaffEntity> findAllByPage(String name, Integer findAllByPage, String phone, String startTime, String endTime, Pageable pageable);
    Page<SignEntity> findSignAllByPage(Long id, String startTime, String endTime, Pageable pageable);
    List<StaffEntity> findAll(StaffEntity staffEntity, Sort sort);

    List<SignEntity> getTodayTimesheetList(Long staffId);

    StaffEntity create(String account,String password) throws IllegalArgumentException;

    StaffEntity updatePassword(StaffEntity staffEntity,String password);
    /**
     * 保存员工
     * */
    StaffEntity save(StaffEntity staffEntity);
    /**
     * 根据员工的姓名查询员工
     * */
    Optional<StaffEntity> findByName(String name);

    Optional<StaffEntity> findByAccount(String account);

    /**
     * 根据员工的id删除员工
     * */
    void deleteById(Long id);

    /**
     * 根据员工的手机号码查询员工
     * */
    Optional<StaffEntity> findByPhone(String phone);

    /**
     * 员工签到
     * */
    SignEntity sign(Long staffId,Integer type);
}
