package com.example.controller.admin;

import com.example.dto.statsManage.StatsQueryDTO;
import com.example.result.Result;
import com.example.service.statsManage.AdminStatsService;
import com.example.vo.stats.TrendVO;
import com.example.vo.statsManage.AdminOverviewVO;
import com.example.vo.statsManage.AuditStatsVO;
import com.example.vo.statsManage.ReportStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "管理端-统计", description = "概览、趋势、审核、举报、导出")
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @Operation(summary = "首页概览")
    @GetMapping("/overview")
    public Result<AdminOverviewVO> overview() {
        return Result.success(adminStatsService.getOverview());
    }

    @Operation(summary = "用户统计趋势")
    @GetMapping("/users")
    public Result<TrendVO> userTrend(StatsQueryDTO query) {
        return Result.success(adminStatsService.getUserTrend(query));
    }

    @Operation(summary = "商品统计趋势")
    @GetMapping("/products")
    public Result<TrendVO> productTrend(StatsQueryDTO query) {
        return Result.success(adminStatsService.getProductTrend(query));
    }

    @Operation(summary = "订单统计趋势")
    @GetMapping("/orders")
    public Result<TrendVO> orderTrend(StatsQueryDTO query) {
        return Result.success(adminStatsService.getOrderTrend(query));
    }

    @Operation(summary = "AI审核统计")
    @GetMapping("/audits")
    public Result<AuditStatsVO> auditStats() {
        return Result.success(adminStatsService.getAuditStats());
    }

    @Operation(summary = "举报统计")
    @GetMapping("/reports")
    public Result<ReportStatsVO> reportStats() {
        return Result.success(adminStatsService.getReportStats());
    }

    @Operation(summary = "导出统计 Excel")
    @GetMapping("/export")
    public void export(
            @Parameter(description = "类型 users/products/orders/audits/reports")
            @RequestParam String type,
            HttpServletResponse response) throws IOException {
        adminStatsService.export(response, type);
    }
}