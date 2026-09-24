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
@Schema(description = "我的数据概览")
public class UserOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品总数")
    private Integer productTotal;

    @Schema(description = "在售商品数")
    private Integer productOnSale;

    @Schema(description = "已售出商品数")
    private Integer productSold;

    @Schema(description = "我买到的订单数")
    private Integer orderBuyTotal;

    @Schema(description = "我卖出的订单数")
    private Integer orderSellTotal;

    @Schema(description = "待处理订单数")
    private Integer orderPending;

    @Schema(description = "当前信用分")
    private Integer creditScore;

    @Schema(description = "收到的评价数")
    private Integer reviewCount;

    @Schema(description = "平均评分")
    private Double avgScore;

    @Schema(description = "收藏商品数")
    private Integer favoriteCount;

    @Schema(description = "未读消息数")
    private Long unreadMessage;
}