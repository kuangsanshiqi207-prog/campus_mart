package com.example.vo.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的评价统计")
public class UserReviewStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "收到评价数")
    private Integer receivedTotal;

    @Schema(description = "发出评价数")
    private Integer sentTotal;

    @Schema(description = "平均分")
    private Double avgScore;

    @Schema(description = "5 星数")
    private Integer score5Count;

    @Schema(description = "4 星数")
    private Integer score4Count;

    @Schema(description = "3 星数")
    private Integer score3Count;

    @Schema(description = "2 星数")
    private Integer score2Count;

    @Schema(description = "1 星数")
    private Integer score1Count;

    @Schema(description = "好评率（4-5星占比）")
    private Double goodRate;
}