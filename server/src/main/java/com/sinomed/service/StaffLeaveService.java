package com.sinomed.service;

import com.sinomed.entity.StaffLeaveEntity;
import com.sinomed.vo.StaffLeaveView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 员工请假：记录性质（类型/事由/起止时间），不改员工登录状态、无审批流。
 */
public interface StaffLeaveService {

    /** 提交请假（校验员工/类型/事由/起止时间） */
    StaffLeaveEntity create(StaffLeaveView view);

    /** 请假分页；staffId 为空查全部（id 倒序） */
    Page<StaffLeaveEntity> findPage(Long staffId, Pageable pageable);

    /** 删除请假记录 */
    void deleteById(Long id);
}
