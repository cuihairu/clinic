package com.sinomed.service;

import com.sinomed.entity.StaffShiftEntity;
import com.sinomed.vo.StaffShiftView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StaffShiftService {
    /**
     * 加排班：staffId + weekday（1-7）+ start/end（HH:mm，start < end）必填；同员工同星期唯一
     */
    StaffShiftEntity save(StaffShiftView view);

    /**
     * 按 id 全量更新；换星期撞同员工已有班次报 400
     */
    StaffShiftEntity update(StaffShiftView view);

    /**
     * 班次详情（联员工姓名）；不存在报 400
     */
    StaffShiftView findById(Long id);

    /**
     * 班次分页：staffId/weekday 可空过滤（按空值分派），按员工、星期排序
     */
    Page<StaffShiftEntity> findPage(Long staffId, Integer weekday, Pageable pageable);

    /**
     * 某员工的整周班次（weekday 升序）——考勤/预约侧取数入口
     */
    java.util.List<StaffShiftEntity> listByStaff(Long staffId);

    /**
     * 删除班次（演示环境口径，无留痕）
     */
    void deleteById(Long id);
}
