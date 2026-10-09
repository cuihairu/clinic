package com.sinomed.service;

import com.sinomed.entity.AppointmentEntity;
import com.sinomed.vo.AppointmentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AppointmentService {
    /**
     * 根据预约的id查询预约
     * */
    Optional<AppointmentEntity> findById(Long id);
    /**
     * 保存预约（新建：status 置 0 待到店）
     * */
    AppointmentEntity save(AppointmentView view);
    /**
     * 根据预约的id删除预约
     * */
    void deleteById(Long id);
    /**
     * 管理端分页；status 为空查全部
     * */
    Page<AppointmentEntity> findPage(Integer status, Pageable pageable);
    /**
     * 状态流转：只允许 0→1（到店接待）与 0→9（取消）；非法流转抛 IllegalArgumentException
     * */
    AppointmentEntity updateStatus(Long id, Integer status);
}
