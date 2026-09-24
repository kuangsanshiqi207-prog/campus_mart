package com.example.vo.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "信用分流水")
public class CreditLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "变动值")
    private Integer delta;

    @Schema(description = "变动前")
    private Integer beforeScore;

    @Schema(description = "变动后")
    private Integer afterScore;

    @Schema(description = "原因")
    private String reason;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "时间")
    private LocalDateTime createTime;
}