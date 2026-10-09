package com.example.mapper.statsManage;

import com.example.vo.statsManage.ReportStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminStatsMapper {

    // ==================== 概览 ====================

    Long countUserTotal();

    Long countUserToday();

    Long countProductTotal();

    Long countProductToday();

    Long countOrderTotal();

    Long countOrderToday();

    Long countReportPending();

    Long countAuditPending();

    // ==================== 趋势 ====================

    List<Map<String, Object>> countUserTrend(@Param("start") LocalDate start,
                                             @Param("end") LocalDate end);

    List<Map<String, Object>> countProductTrend(@Param("start") LocalDate start,
                                                @Param("end") LocalDate end);

    List<Map<String, Object>> countOrderTrend(@Param("start") LocalDate start,
                                              @Param("end") LocalDate end);

    // ==================== 举报统计 ====================

    ReportStatsVO countReportStats();

    // ==================== 审核统计 ====================

    Long countAuditTotal();

    Long countAuditPass();

    Long countAuditReject();
}