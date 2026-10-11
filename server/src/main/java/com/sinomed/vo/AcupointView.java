package com.sinomed.vo;

import com.sinomed.entity.AcupointEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 穴位视图：字段与 acupoints 表对齐。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "穴位")
public class AcupointView {
    @Schema(title = "穴位id", example = "1")
    private Long id;

    @Schema(title = "穴位名", example = "足三里")
    private String name;

    @Schema(title = "拼音检索码（全拼小写）", example = "zusanli")
    private String pinyin;

    @Schema(title = "归经", example = "足阳明胃经")
    private String meridian;

    @Schema(title = "体表定位")
    private String location;

    @Schema(title = "主治简述")
    private String indication;

    @Schema(title = "创建时间", example = "2026-09-21 10:00:00")
    private Date createTime;

    public static AcupointView FromEntity(AcupointEntity entity) {
        return AcupointView.builder()
                .id(entity.getId())
                .name(entity.getName())
                .pinyin(entity.getPinyin())
                .meridian(entity.getMeridian())
                .location(entity.getLocation())
                .indication(entity.getIndication())
                .createTime(entity.getCreateTime())
                .build();
    }
}
