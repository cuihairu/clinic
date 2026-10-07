package com.sinomed.service;

import com.sinomed.entity.AdMaterialEntity;
import com.sinomed.entity.AdScheduleEntity;
import com.sinomed.entity.AdScreenEntity;
import com.sinomed.vo.MediaUploadView;
import com.sinomed.vo.PlaylistView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface AdsService {
    // ---------- 素材 ----------
    Page<AdMaterialEntity> findMaterials(AdMaterialEntity example, Pageable pageable);

    Optional<AdMaterialEntity> findMaterialById(Long id);

    AdMaterialEntity saveMaterial(AdMaterialEntity entity);

    void deleteMaterialById(Long id);

    MediaUploadView uploadMaterial(MultipartFile file);

    // ---------- 屏 ----------
    Page<AdScreenEntity> findScreens(AdScreenEntity example, Pageable pageable);

    Optional<AdScreenEntity> findScreenById(Long id);

    AdScreenEntity saveScreen(AdScreenEntity entity);

    void deleteScreenById(Long id);

    // ---------- 排期 ----------
    Page<AdScheduleEntity> findSchedules(AdScheduleEntity example, Pageable pageable);

    Optional<AdScheduleEntity> findScheduleById(Long id);

    AdScheduleEntity saveSchedule(AdScheduleEntity entity);

    void deleteScheduleById(Long id);

    // ---------- 下发 ----------
    /**
     * 按屏 code 计算播放序列并顺带心跳。
     *
     * @param screenCode 屏标识
     */
    PlaylistView playlist(String screenCode);
}
