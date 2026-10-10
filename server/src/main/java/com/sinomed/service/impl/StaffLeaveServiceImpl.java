package com.sinomed.service.impl;

import com.sinomed.entity.StaffLeaveEntity;
import com.sinomed.repository.StaffLeaveRepository;
import com.sinomed.repository.StaffRepository;
import com.sinomed.service.StaffLeaveService;
import com.sinomed.vo.StaffLeaveView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StaffLeaveServiceImpl implements StaffLeaveService {

    private final StaffLeaveRepository staffLeaveRepository;
    private final StaffRepository staffRepository;

    /**
     * 提交请假：员工必须在职存在、类型 0/1、事由非空、起止时间齐备且结束不早于起始
     */
    @Override
    @Transactional
    public StaffLeaveEntity create(StaffLeaveView view) {
        if (view == null || view.getStaffId() == null) {
            throw new IllegalArgumentException("员工id不能为空");
        }
        staffRepository.findById(view.getStaffId())
                .orElseThrow(() -> new IllegalArgumentException("员工不存在：" + view.getStaffId()));
        Integer leaveType = view.getLeaveType();
        if (leaveType == null || leaveType != 0 && leaveType != 1) {
            throw new IllegalArgumentException("类型无效：0 病假 / 1 事假");
        }
        if (view.getReason() == null || view.getReason().isBlank()) {
            throw new IllegalArgumentException("事由不能为空");
        }
        if (view.getStartTime() == null || view.getEndTime() == null) {
            throw new IllegalArgumentException("起止时间不能为空");
        }
        if (view.getEndTime().before(view.getStartTime())) {
            throw new IllegalArgumentException("结束时间不能早于起始时间");
        }

        StaffLeaveEntity leave = new StaffLeaveEntity();
        leave.setStaffId(view.getStaffId());
        leave.setLeaveType(leaveType);
        leave.setReason(view.getReason().trim());
        leave.setStartTime(view.getStartTime());
        leave.setEndTime(view.getEndTime());
        return staffLeaveRepository.save(leave);
    }

    /**
     * 请假分页；staffId 为空查全部（id 倒序由 Pageable 传入）
     */
    @Override
    public Page<StaffLeaveEntity> findPage(Long staffId, Pageable pageable) {
        if (staffId == null) {
            return staffLeaveRepository.findAll(pageable);
        }
        return staffLeaveRepository.findByStaffId(staffId, pageable);
    }

    /**
     * 删除请假记录
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        staffLeaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在：" + id));
        staffLeaveRepository.deleteById(id);
    }
}
