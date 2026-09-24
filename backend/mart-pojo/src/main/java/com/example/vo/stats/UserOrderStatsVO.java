package com.example.vo.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的交易统计")
public class UserOrderStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "买到的总数")
    private Integer buyTotal;

    @Schema(description = "卖出的总数")
    private Integer sellTotal;

    @Schema(description = "买入已完成")
    private Integer buyCompleted;

    @Schema(description = "卖出已完成")
    private Integer sellCompleted;

    @Schema(description = "买入总金额")
    private BigDecimal buyAmount;

    @Schema(description = "卖出总金额")
    private BigDecimal sellAmount;

    @Schema(description = "待处理")
    private Integer pending;

    @Schema(description = "退款中")
    private Integer refunding;

    @Schema(description = "纠纷中")
    private Integer dispute;
}