package com.example.controller.admin;

import com.example.dto.reportManage.AdminAppealQueryDTO;
import com.example.dto.reportManage.AdminReportQueryDTO;
import com.example.dto.reportManage.AppealHandleDTO;
import com.example.dto.reportManage.ReportHandleDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.reportManage.AdminReportService;
import com.example.vo.report.ReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-举报管理", description = "举报列表、详情、处理、申诉列表、申诉处理")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    // ==================== 举报 ====================

    @Operation(summary = "举报列表")
    @GetMapping("/reports")
    public Result<PageResult> listReports(AdminReportQueryDTO query) {
        return Result.success(adminReportService.listReports(query));
    }

    @Operation(summary = "举报详情")
    @GetMapping("/reports/{id}")
    public Result<ReportVO> reportDetail(
            @Parameter(description = "举报ID") @PathVariable Long id) {
        return Result.success(adminReportService.getReportDetail(id));
    }

    @Operation(summary = "处理举报")
    @PutMapping("/reports/{id}/handle")
    public Result<Void> handleReport(
            @Parameter(description = "举报ID") @PathVariable Long id,
            @RequestBody @Valid ReportHandleDTO dto) {
        adminReportService.handleReport(id, dto);
        return Result.success();
    }

    // ==================== 申诉 ====================

    @Operation(summary = "申诉列表")
    @GetMapping("/appeals")
    public Result<PageResult> listAppeals(AdminAppealQueryDTO query) {
        return Result.success(adminReportService.listAppeals(query));
    }

    @Operation(summary = "处理申诉")
    @PutMapping("/appeals/{id}/handle")
    public Result<Void> handleAppeal(
            @Parameter(description = "申诉ID") @PathVariable Long id,
            @RequestBody @Valid AppealHandleDTO dto) {
        adminReportService.handleAppeal(id, dto);
        return Result.success();
    }
}