package com.sinomed.vo;

import com.sinomed.entity.FormulaEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 方剂视图：字段与 formulas 表对齐；herbs 为方剂药味组成。
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "方剂")
public class FormulaView {
    @Schema(title = "方剂id", example = "1")
    private Long id;

    @Schema(title = "方名", example = "逍遥散")
    private String name;

    @Schema(title = "拼音检索码（全拼小写）", example = "xiaoyaosan")
    private String pinyin;

    @Schema(title = "出处", example = "太平惠民和剂局方")
    private String source;

    @Schema(title = "功效主治简述")
    private String indication;

    @Schema(title = "药味组成（按 sort 排序）")
    private List<PrescriptionItemView> herbs;

    @Schema(title = "创建时间", example = "2026-09-21 10:00:00")
    private Date createTime;

    public static FormulaView FromEntity(FormulaEntity entity) {
        return FormulaView.builder()
                .id(entity.getId())
                .name(entity.getName())
                .pinyin(entity.getPinyin())
                .source(entity.getSource())
                .indication(entity.getIndication())
                .createTime(entity.getCreateTime())
                .build();
    }
}
