package com.example.vo.statsManage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@Schema(description = "AI 审核统计")
public class AuditStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "审核总数")
    private Long total;

    @Schema(description = "通过数")
    private Long passCount;

    @Schema(description = "人工复核数")
    private Long manualCount;

    @Schema(description = "拒绝数")
    private Long rejectCount;

    @Schema(description = "平均置信度")
    private Double avgConfidence;
}