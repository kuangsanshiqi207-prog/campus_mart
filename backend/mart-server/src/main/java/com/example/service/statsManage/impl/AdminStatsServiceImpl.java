package com.example.service.statsManage.impl;

import jakarta.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import com.alibaba.excel.EasyExcel;
import com.example.dto.statsManage.StatsQueryDTO;
import com.example.mapper.statsManage.AdminStatsMapper;
import com.example.service.statsManage.AdminStatsService;
import com.example.vo.stats.TrendVO;
import com.example.vo.statsManage.AdminOverviewVO;
import com.example.vo.statsManage.AuditStatsVO;
import com.example.vo.statsManage.ReportStatsVO;
import com.example.vo.statsManage.TrendExcelVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    private final AdminStatsMapper adminStatsMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int DEFAULT_DAYS = 30;

    // ==================== 概览 ====================

    @Override
    public AdminOverviewVO getOverview() {
        return AdminOverviewVO.builder()
                .userTotal(nvl(adminStatsMapper.countUserTotal()))
                .userToday(nvl(adminStatsMapper.countUserToday()))
                .productTotal(nvl(adminStatsMapper.countProductTotal()))
                .productToday(nvl(adminStatsMapper.countProductToday()))
                .orderTotal(nvl(adminStatsMapper.countOrderTotal()))
                .orderToday(nvl(adminStatsMapper.countOrderToday()))
                .reportPending(nvl(adminStatsMapper.countReportPending()))
                .auditPending(nvl(adminStatsMapper.countAuditPending()))
                .build();
    }

    // ==================== 趋势 ====================

    @Override
    public TrendVO getUserTrend(StatsQueryDTO query) {
        LocalDate end = (query != null && query.getEndDate() != null)
                ? query.getEndDate() : LocalDate.now();
        LocalDate start = (query != null && query.getStartDate() != null)
                ? query.getStartDate() : end.minusDays(DEFAULT_DAYS - 1);

        List<Map<String, Object>> raw = adminStatsMapper.countUserTrend(start, end);
        return fillTrend(start, end, raw);
    }

    @Override
    public TrendVO getProductTrend(StatsQueryDTO query) {
        LocalDate end = (query != null && query.getEndDate() != null)
                ? query.getEndDate() : LocalDate.now();
        LocalDate start = (query != null && query.getStartDate() != null)
                ? query.getStartDate() : end.minusDays(DEFAULT_DAYS - 1);

        List<Map<String, Object>> raw = adminStatsMapper.countProductTrend(start, end);
        return fillTrend(start, end, raw);
    }

    @Override
    public TrendVO getOrderTrend(StatsQueryDTO query) {
        LocalDate end = (query != null && query.getEndDate() != null)
                ? query.getEndDate() : LocalDate.now();
        LocalDate start = (query != null && query.getStartDate() != null)
                ? query.getStartDate() : end.minusDays(DEFAULT_DAYS - 1);

        List<Map<String, Object>> raw = adminStatsMapper.countOrderTrend(start, end);
        return fillTrend(start, end, raw);
    }

    // ==================== 举报统计 ====================

    @Override
    public ReportStatsVO getReportStats() {
        ReportStatsVO vo = adminStatsMapper.countReportStats();
        if (vo == null) {
            return ReportStatsVO.builder()
                    .total(0L).pending(0L).valid(0L).invalid(0L).handleRate(0.0)
                    .build();
        }
        if (vo.getHandleRate() != null) {
            vo.setHandleRate(Math.round(vo.getHandleRate() * 100.0) / 100.0);
        } else {
            vo.setHandleRate(0.0);
        }
        return vo;
    }

    // ==================== 审核统计 ====================

    @Override
    public AuditStatsVO getAuditStats() {
        Long total = nvl(adminStatsMapper.countAuditTotal());
        Long passCount = nvl(adminStatsMapper.countAuditPass());
        Long rejectCount = nvl(adminStatsMapper.countAuditReject());

        return AuditStatsVO.builder()
                .total(total)
                .passCount(passCount)
                .rejectCount(rejectCount)
                .manualCount(0L)
                .avgConfidence(0.0)
                .build();
    }

    // ==================== 导出 ====================

    @Override
    public void export(HttpServletResponse response, String type) throws IOException {
        List<TrendExcelVO> data = buildExportData(type);

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(type);

            // 表头
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("项目");
            header.createCell(1).setCellValue("数值");

            // 数据
            for (int i = 0; i < data.size(); i++) {
                TrendExcelVO item = data.get(i);
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getLabel());
                row.createCell(1).setCellValue(String.valueOf(item.getValue()));
            }

            // 写回 response
            String fileName = "stats_" + type + "_" + LocalDate.now() + ".xlsx";
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    private List<TrendExcelVO> buildExportData(String type) {
        List<TrendExcelVO> data = new ArrayList<>();

        if ("users".equals(type)) {
            TrendVO trend = getUserTrend(defaultQuery());
            fillTrendData(data, trend, "用户");
        } else if ("products".equals(type)) {
            TrendVO trend = getProductTrend(defaultQuery());
            fillTrendData(data, trend, "商品");
        } else if ("orders".equals(type)) {
            TrendVO trend = getOrderTrend(defaultQuery());
            fillTrendData(data, trend, "订单");
        } else if ("reports".equals(type)) {
            ReportStatsVO stats = getReportStats();
            data.add(new TrendExcelVO("举报总数", stats.getTotal()));
            data.add(new TrendExcelVO("待处理", stats.getPending()));
            data.add(new TrendExcelVO("举报成立", stats.getValid()));
            data.add(new TrendExcelVO("举报无效", stats.getInvalid()));
            data.add(new TrendExcelVO("处理率(%)", stats.getHandleRate()));
        } else if ("audits".equals(type)) {
            AuditStatsVO stats = getAuditStats();
            data.add(new TrendExcelVO("审核总数", stats.getTotal()));
            data.add(new TrendExcelVO("通过", stats.getPassCount()));
            data.add(new TrendExcelVO("拒绝", stats.getRejectCount()));
            data.add(new TrendExcelVO("人工复核", stats.getManualCount()));
            data.add(new TrendExcelVO("平均置信度", stats.getAvgConfidence()));
        }

        return data;
    }

    private void fillTrendData(List<TrendExcelVO> data, TrendVO trend, String prefix) {
        List<LocalDate> dates = trend.getDates();
        List<Long> values = trend.getValues();
        for (int i = 0; i < dates.size(); i++) {
            data.add(new TrendExcelVO(prefix + " - " + dates.get(i), values.get(i)));
        }
    }

    // ==================== 私有方法：补齐日期 ====================

    /**
     * 把 SQL 查到的稀疏数据补齐成完整日期序列
     *
     * @param start 开始日期
     * @param end   结束日期
     * @param raw   SQL 结果，每行是 {date: 日期, value: 数量}
     * @return      TrendVO，dates 和 values 等长
     */
    private TrendVO fillTrend(LocalDate start, LocalDate end,
                              List<Map<String, Object>> raw) {

        // 1. SQL 结果 → Map<"2026-09-24", 5>
        Map<String, Long> dataMap = new HashMap<>();
        for (Map<String, Object> row : raw) {
            Object dateObj = row.get("date");
            Object valueObj = row.get("value");
            if (dateObj == null || valueObj == null) continue;
            dataMap.put(dateObj.toString(), ((Number) valueObj).longValue());
        }

        // 2. 从 start 到 end，一天一天补齐，没有数据填 0
        List<LocalDate> dates = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            String key = cursor.format(DATE_FMT);
            values.add(dataMap.getOrDefault(key, 0L));
            cursor = cursor.plusDays(1);
        }

        return TrendVO.builder()
                .dates(dates)
                .values(values)
                .build();
    }

    private StatsQueryDTO defaultQuery() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(DEFAULT_DAYS - 1);
        StatsQueryDTO query = new StatsQueryDTO();
        query.setStartDate(start);
        query.setEndDate(end);
        return query;
    }

    private Long nvl(Long v) {
        return v == null ? 0L : v;
    }
}