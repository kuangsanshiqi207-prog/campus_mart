package com.example.vo.statsManage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@Schema(description = "举报统计")
public class ReportStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "举报总数")
    private Long total;

    @Schema(description = "待处理")
    private Long pending;

    @Schema(description = "举报成立")
    private Long valid;

    @Schema(description = "举报无效")
    private Long invalid;

    @Schema(description = "处理率（百分比）")
    private Double handleRate;
}