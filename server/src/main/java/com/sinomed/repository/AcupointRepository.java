package com.sinomed.repository;

import com.sinomed.entity.AcupointEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AcupointRepository extends JpaRepository<AcupointEntity, Long> {
    Optional<AcupointEntity> findByName(String name);

    /** 穴位检索：穴名或拼音检索码模糊匹配，id 倒序（keyword 传空串即全量） */
    @Query("select a from AcupointEntity a where a.name like concat('%', :keyword, '%') "
            + "or a.pinyin like concat('%', :keyword, '%') order by a.id desc")
    Page<AcupointEntity> search(@Param("keyword") String keyword, Pageable pageable);
}
