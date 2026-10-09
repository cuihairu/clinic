package com.sinomed.service.impl;

import com.sinomed.entity.AppointmentEntity;
import com.sinomed.repository.AppointmentRepository;
import com.sinomed.service.AppointmentService;
import com.sinomed.vo.AppointmentView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    /**
     * 根据预约的id查询预约
     *
     * @param id
     */
    @Override
    public Optional<AppointmentEntity> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    /**
     * 保存预约：新建一律 status=0 待到店；创建/更新时间由审计维护
     */
    @Override
    public AppointmentEntity save(AppointmentView view) {
        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setCustomerId(view.getCustomerId());
        appointment.setItemId(view.getItemId());
        appointment.setStaffId(view.getStaffId());
        appointment.setStartTime(view.getStartTime());
        appointment.setDuration(view.getDuration());
        appointment.setRemark(view.getRemark());
        appointment.setStatus(0);
        return appointmentRepository.save(appointment);
    }

    /**
     * 根据预约的id删除预约
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        appointmentRepository.deleteById(id);
    }

    /**
     * 管理端分页；status 为空查全部
     */
    @Override
    public Page<AppointmentEntity> findPage(Integer status, Pageable pageable) {
        if (status == null) {
            return appointmentRepository.findAll(pageable);
        }
        return appointmentRepository.findByStatus(status, pageable);
    }

    /**
     * 状态流转：只允许 0→1（到店接待）与 0→9（取消）
     */
    @Override
    public AppointmentEntity updateStatus(Long id, Integer status) {
        AppointmentEntity appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预约不存在：" + id));
        Integer current = appointment.getStatus();
        boolean allowed = (current == null || current == 0) && (status == 1 || status == 9);
        if (!allowed) {
            throw new IllegalArgumentException("预约状态不允许从 " + current + " 流转到 " + status);
        }
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
}
