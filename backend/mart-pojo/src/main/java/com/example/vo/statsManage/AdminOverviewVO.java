package com.example.vo.statsManage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@Schema(description = "管理端首页概览")
public class AdminOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户总数")
    private Long userTotal;

    @Schema(description = "今日新增用户")
    private Long userToday;

    @Schema(description = "商品总数")
    private Long productTotal;

    @Schema(description = "今日新增商品")
    private Long productToday;

    @Schema(description = "订单总数")
    private Long orderTotal;

    @Schema(description = "今日新增订单")
    private Long orderToday;

    @Schema(description = "待处理举报")
    private Long reportPending;

    @Schema(description = "待审核商品")
    private Long auditPending;
}