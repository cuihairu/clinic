package com.sinomed.vo;


import com.sinomed.entity.AdScreenEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "广告屏")
public class AdScreenView {
    @Schema(title = "屏id", example = "1")
    private Long id;

    @NotNull
    @Schema(title = "屏标识", description = "唯一，平板配置里填", requiredMode = Schema.RequiredMode.REQUIRED, example = "PAD-01")
    private String code;

    @NotNull
    @Schema(title = "屏名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一楼候诊区")
    private String name;

    @Schema(title = "位置", description = "候诊区 / 诊室", example = "一楼候诊区")
    private String location;

    @NotNull
    @Schema(title = "是否启用", description = "1 启用、0 停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer enabled;

    @Schema(title = "最近心跳", example = "2026-10-07 09:30:00")
    private Date lastSeenAt;

    @Schema(title = "创建时间", example = "2026-10-01 12:00:00")
    private Date createTime;

    @Schema(title = "更新时间", example = "2026-10-07 09:00:00")
    private Date updateTime;

    public AdScreenEntity ToAdScreenEntity() {
        AdScreenEntity entity = new AdScreenEntity();
        entity.setId(id);
        entity.setCode(code);
        entity.setName(name);
        entity.setLocation(location);
        entity.setEnabled(enabled);
        return entity;
    }

    public static AdScreenView FromAdScreenEntity(AdScreenEntity entity) {
        return AdScreenView.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .location(entity.getLocation())
                .enabled(entity.getEnabled())
                .lastSeenAt(entity.getLastSeenAt())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
