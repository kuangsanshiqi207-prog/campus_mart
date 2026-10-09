package com.example.service.report;

import com.example.dto.report.AppealCreateDTO;
import com.example.dto.report.ReportCreateDTO;
import com.example.result.PageResult;
import com.example.vo.report.ReportVO;

public interface ReportService {

    Long createReport(ReportCreateDTO dto);

    PageResult listMyReports(Integer pageNum, Integer pageSize);

    ReportVO getReportDetail(Long id);

    Long createAppeal(Long reportId, AppealCreateDTO dto);
}