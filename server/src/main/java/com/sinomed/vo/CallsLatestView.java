package com.sinomed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(title = "平板叫号拉取返回")
public class CallsLatestView {
    @Schema(title = "最新游标", description = "平板用它作下次 since；无新记录时等于传入 since")
    private Long since;

    @Schema(title = "新叫号列表", description = "since 之后的记录，按 id 升序")
    private List<QueueCallView> calls;
}
