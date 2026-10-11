package com.sinomed.service.impl;

import com.sinomed.entity.AcupointEntity;
import com.sinomed.repository.AcupointRepository;
import com.sinomed.service.AcupointService;
import com.sinomed.vo.AcupointView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 穴位字典实现：穴名唯一（1–10 字）；拼音检索码为全拼小写字母；归经必填。
 * 穴位为参考资料，接诊单取穴字段仍为自由文本，字典只做选穴辅助。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AcupointServiceImpl implements AcupointService {

    private final AcupointRepository acupointRepository;

    @Override
    @Transactional
    public AcupointEntity save(AcupointView view) {
        String name = validName(view, null);
        String pinyin = validPinyin(view == null ? null : view.getPinyin());
        String meridian = validMeridian(view);

        AcupointEntity point = new AcupointEntity();
        point.setName(name);
        point.setPinyin(pinyin);
        point.setMeridian(meridian);
        point.setLocation(trimOrNull(view.getLocation()));
        point.setIndication(trimOrNull(view.getIndication()));
        return acupointRepository.save(point);
    }

    @Override
    @Transactional
    public AcupointEntity update(AcupointView view) {
        if (view == null || view.getId() == null) {
            throw new IllegalArgumentException("穴位id不能为空");
        }
        AcupointEntity point = acupointRepository.findById(view.getId())
                .orElseThrow(() -> new IllegalArgumentException("穴位不存在：" + view.getId()));
        String name = validName(view, point.getId());
        String pinyin = validPinyin(view.getPinyin());
        String meridian = validMeridian(view);

        point.setName(name);
        point.setPinyin(pinyin);
        point.setMeridian(meridian);
        point.setLocation(trimOrNull(view.getLocation()));
        point.setIndication(trimOrNull(view.getIndication()));
        return acupointRepository.save(point);
    }

    @Override
    public AcupointView findById(Long id) {
        AcupointEntity point = acupointRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("穴位不存在：" + id));
        return AcupointView.FromEntity(point);
    }

    @Override
    public Page<AcupointEntity> findPage(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return acupointRepository.search(kw, pageable);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        acupointRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("穴位不存在：" + id));
        acupointRepository.deleteById(id);
    }

    /** 穴名校验：非空、限 10 字、唯一（更新时放过自身） */
    private String validName(AcupointView view, Long selfId) {
        if (view == null || view.getName() == null || view.getName().isBlank()) {
            throw new IllegalArgumentException("穴位名不能为空");
        }
        String name = view.getName().trim();
        if (name.length() > 10) {
            throw new IllegalArgumentException("穴位名限 10 字内");
        }
        acupointRepository.findByName(name)
                .filter(other -> !other.getId().equals(selfId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("穴位名已存在：" + name);
                });
        return name;
    }

    /** 拼音检索码校验：非空、全拼小写字母（检索按 contains 匹配） */
    private String validPinyin(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("拼音检索码不能为空");
        }
        String pinyin = raw.trim().toLowerCase();
        if (!pinyin.matches("[a-z]+")) {
            throw new IllegalArgumentException("拼音检索码只能为小写字母：" + raw);
        }
        return pinyin;
    }

    /** 归经校验：非空、限 20 字 */
    private String validMeridian(AcupointView view) {
        if (view == null || view.getMeridian() == null || view.getMeridian().isBlank()) {
            throw new IllegalArgumentException("归经不能为空");
        }
        String meridian = view.getMeridian().trim();
        if (meridian.length() > 20) {
            throw new IllegalArgumentException("归经限 20 字内");
        }
        return meridian;
    }

    private String trimOrNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }
}
