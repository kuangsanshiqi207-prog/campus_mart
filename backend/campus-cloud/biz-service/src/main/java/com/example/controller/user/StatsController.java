package com.example.controller.user;

import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.stats.StatsService;
import com.example.vo.product.UserProductStatsVO;
import com.example.vo.stats.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-个人统计", description = "我的数据、商品、交易、评价、信用分")
@RestController
@RequestMapping("/user/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "我的数据概览")
    @GetMapping("/overview")
    public Result<UserOverviewVO> overview() {
        return Result.success(statsService.getOverview());
    }

    @Operation(summary = "我的商品统计")
    @GetMapping("/shop")
    public Result<UserProductStatsVO> shop() {
        return Result.success(statsService.getMyProductStats());
    }

    @Operation(summary = "我的交易统计")
    @GetMapping("/orders")
    public Result<UserOrderStatsVO> orders() {
        return Result.success(statsService.getMyOrderStats());
    }

    @Operation(summary = "我的评价统计")
    @GetMapping("/reviews")
    public Result<UserReviewStatsVO> reviews() {
        return Result.success(statsService.getMyReviewStats());
    }

    @Operation(summary = "信用分流水")
    @GetMapping("/credit")
    public Result<PageResult> credit(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(statsService.listCreditLogs(pageNum, pageSize));
    }

    @Operation(summary = "信用分趋势")
    @GetMapping("/credit/trend")
    public Result<TrendVO> creditTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(statsService.getCreditTrend(days));
    }

    @Operation(summary = "交易趋势")
    @GetMapping("/orders/trend")
    public Result<TrendVO> orderTrend(
            @Parameter(description = "视角 buy/sell") @RequestParam(defaultValue = "buy") String type,
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(statsService.getOrderTrend(type, days));
    }
}