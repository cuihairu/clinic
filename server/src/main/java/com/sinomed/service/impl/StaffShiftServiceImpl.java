package com.sinomed.service.impl;

import com.sinomed.entity.StaffEntity;
import com.sinomed.entity.StaffShiftEntity;
import com.sinomed.repository.StaffRepository;
import com.sinomed.repository.StaffShiftRepository;
import com.sinomed.service.StaffShiftService;
import com.sinomed.vo.StaffShiftView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 员工周期班表实现：员工 × 星期（1-7）× HH:mm 班次，同员工同星期唯一。
 * 班表只做排班参考，考勤打卡仍以 signs 为准；时段校验 HH:mm 且 start < end。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StaffShiftServiceImpl implements StaffShiftService {

    private final StaffShiftRepository shiftRepository;
    private final StaffRepository staffRepository;

    @Override
    @Transactional
    public StaffShiftEntity save(StaffShiftView view) {
        Long staffId = validStaff(view == null ? null : view.getStaffId());
        Integer weekday = validWeekday(view.getWeekday());
        String start = validTime(view.getStart(), "班次开始时间格式无效：");
        String end = validTime(view.getEnd(), "班次结束时间格式无效：");
        if (start.compareTo(end) >= 0) {
            throw new IllegalArgumentException("班次结束时间须晚于开始时间");
        }
        validDuplicate(staffId, weekday, null);

        StaffShiftEntity shift = new StaffShiftEntity();
        shift.setStaffId(staffId);
        shift.setWeekday(weekday);
        shift.setStart(start);
        shift.setEnd(end);
        return shiftRepository.save(shift);
    }

    @Override
    @Transactional
    public StaffShiftEntity update(StaffShiftView view) {
        if (view == null || view.getId() == null) {
            throw new IllegalArgumentException("班次id不能为空");
        }
        StaffShiftEntity shift = shiftRepository.findById(view.getId())
                .orElseThrow(() -> new IllegalArgumentException("班次不存在：" + view.getId()));
        Long staffId = validStaff(view.getStaffId());
        Integer weekday = validWeekday(view.getWeekday());
        String start = validTime(view.getStart(), "班次开始时间格式无效：");
        String end = validTime(view.getEnd(), "班次结束时间格式无效：");
        if (start.compareTo(end) >= 0) {
            throw new IllegalArgumentException("班次结束时间须晚于开始时间");
        }
        validDuplicate(staffId, weekday, shift.getId());

        shift.setStaffId(staffId);
        shift.setWeekday(weekday);
        shift.setStart(start);
        shift.setEnd(end);
        return shiftRepository.save(shift);
    }

    @Override
    public StaffShiftView findById(Long id) {
        StaffShiftEntity shift = shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("班次不存在：" + id));
        return StaffShiftView.FromEntity(shift, staffName(shift.getStaffId()));
    }

    @Override
    public Page<StaffShiftEntity> findPage(Long staffId, Integer weekday, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "staffId", "weekday", "start"));
        if (staffId != null && weekday != null) {
            return shiftRepository.findByStaffIdAndWeekday(staffId, weekday, pageRequest);
        }
        if (staffId != null) {
            return shiftRepository.findByStaffId(staffId, pageRequest);
        }
        if (weekday != null) {
            return shiftRepository.findByWeekday(weekday, pageRequest);
        }
        return shiftRepository.findAll(pageRequest);
    }

    @Override
    public List<StaffShiftEntity> listByStaff(Long staffId) {
        return shiftRepository.findByStaffIdOrderByWeekdayAsc(staffId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("班次不存在：" + id));
        shiftRepository.deleteById(id);
    }

    private String staffName(Long staffId) {
        return staffRepository.findById(staffId).map(StaffEntity::getName).orElse(null);
    }

    /** 员工校验：必须已存在 */
    private Long validStaff(Long staffId) {
        if (staffId == null) {
            throw new IllegalArgumentException("员工不能为空");
        }
        staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("员工不存在：" + staffId));
        return staffId;
    }

    /** 星期校验：1-7 */
    private Integer validWeekday(Integer weekday) {
        if (weekday == null || weekday < 1 || weekday > 7) {
            throw new IllegalArgumentException("星期无效：" + weekday + "（应为 1-7）");
        }
        return weekday;
    }

    /** 时段校验：HH:mm（小时 00-23、分钟 00-59），返回 trim 后文本 */
    private String validTime(String raw, String label) {
        String text = raw == null ? "" : raw.trim();
        if (!text.matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException(label + raw + "（应为 HH:mm）");
        }
        int hour = Integer.parseInt(text.substring(0, 2));
        int minute = Integer.parseInt(text.substring(3, 5));
        if (hour > 23 || minute > 59) {
            throw new IllegalArgumentException(label + raw + "（应为 HH:mm）");
        }
        return text;
    }

    /** 唯一性校验：同员工同星期仅一条班次（更新时放过自身） */
    private void validDuplicate(Long staffId, Integer weekday, Long selfId) {
        shiftRepository.findByStaffIdAndWeekday(staffId, weekday)
                .filter(other -> !other.getId().equals(selfId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("员工" + StaffShiftView.weekdayText(weekday) + "已有班次");
                });
    }
}
