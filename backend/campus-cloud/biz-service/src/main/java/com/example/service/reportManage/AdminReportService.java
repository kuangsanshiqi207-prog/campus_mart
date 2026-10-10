package com.example.service.reportManage;

import com.example.dto.reportManage.AdminAppealQueryDTO;
import com.example.dto.reportManage.AdminReportQueryDTO;
import com.example.dto.reportManage.AppealHandleDTO;
import com.example.dto.reportManage.ReportHandleDTO;
import com.example.result.PageResult;
import com.example.vo.ai.AiAuditVO;
import com.example.vo.report.AppealVO;
import com.example.vo.report.ReportVO;

public interface AdminReportService {

    PageResult listReports(AdminReportQueryDTO query);

    ReportVO getReportDetail(Long id);

    AiAuditVO aiAudit(Long id);

    void handleReport(Long id, ReportHandleDTO dto);

    PageResult listAppeals(AdminAppealQueryDTO query);

    void handleAppeal(Long id, AppealHandleDTO dto);
}