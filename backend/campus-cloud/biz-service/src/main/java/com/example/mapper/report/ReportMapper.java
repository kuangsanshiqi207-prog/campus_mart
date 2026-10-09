package com.example.mapper.report;

import com.example.dto.reportManage.AdminReportQueryDTO;
import com.example.entity.Report;
import com.example.vo.report.ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    Report getById(Long id);

    Report getByReporterAndTarget(@Param("reporterId") Long reporterId,
                                  @Param("targetType") String targetType,
                                  @Param("targetId") Long targetId);

    void insert(Report report);

    ReportVO getReportVOById(Long id);

    List<ReportVO> listByReporter(@Param("reporterId") Long reporterId,
                                  @Param("offset") Integer offset,
                                  @Param("limit") Integer limit);

    Long countByReporter(Long reporterId);

    List<Report> listAdmin(@Param("query") AdminReportQueryDTO query,
                           @Param("offset") Integer offset,
                           @Param("limit") Integer limit);

    Long countAdmin(@Param("query") AdminReportQueryDTO query);

    void handle(@Param("id") Long id,
                @Param("status") String status,
                @Param("action") String action,
                @Param("handleReason") String handleReason,
                @Param("handlerId") Long handlerId);
}