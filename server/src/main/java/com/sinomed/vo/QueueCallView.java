package com.sinomed.vo;


import com.sinomed.entity.QueueCallEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "叫号记录")
public class QueueCallView {
    @Schema(title = "记录id（同时作平板拉取游标）", example = "5")
    private Long id;

    @Schema(title = "定向屏id", description = "为空 = 全部屏", example = "1")
    private Long screenId;

    @NotNull
    @Schema(title = "号码", requiredMode = Schema.RequiredMode.REQUIRED, example = "08")
    private String number;

    @NotNull
    @Schema(title = "诊室", requiredMode = Schema.RequiredMode.REQUIRED, example = "第二诊室")
    private String room;

    @Schema(title = "脱敏姓名", example = "张*")
    private String patientMasked;

    @Schema(title = "状态", description = "0 待叫、1 已叫", example = "1")
    private Integer status;

    @Schema(title = "叫号时间", example = "2026-10-07 14:00:00")
    private Date calledAt;

    public QueueCallEntity ToQueueCallEntity() {
        QueueCallEntity entity = new QueueCallEntity();
        entity.setId(id);
        entity.setScreenId(screenId);
        entity.setNumber(number);
        entity.setRoom(room);
        entity.setPatientMasked(patientMasked);
        entity.setStatus(status);
        entity.setCalledAt(calledAt);
        return entity;
    }

    public static QueueCallView FromQueueCallEntity(QueueCallEntity entity) {
        return QueueCallView.builder()
                .id(entity.getId())
                .screenId(entity.getScreenId())
                .number(entity.getNumber())
                .room(entity.getRoom())
                .patientMasked(entity.getPatientMasked())
                .status(entity.getStatus())
                .calledAt(entity.getCalledAt())
                .build();
    }
}
