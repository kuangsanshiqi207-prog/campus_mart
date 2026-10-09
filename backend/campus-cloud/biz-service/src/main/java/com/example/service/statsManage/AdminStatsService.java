package com.example.service.statsManage;

import com.example.dto.statsManage.StatsQueryDTO;
import com.example.vo.stats.TrendVO;
import com.example.vo.statsManage.AdminOverviewVO;
import com.example.vo.statsManage.AuditStatsVO;
import com.example.vo.statsManage.ReportStatsVO;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AdminStatsService {

    AdminOverviewVO getOverview();

    TrendVO getUserTrend(StatsQueryDTO query);

    TrendVO getProductTrend(StatsQueryDTO query);

    TrendVO getOrderTrend(StatsQueryDTO query);

    ReportStatsVO getReportStats();

    AuditStatsVO getAuditStats();

    void export(HttpServletResponse response, String type) throws IOException;
}