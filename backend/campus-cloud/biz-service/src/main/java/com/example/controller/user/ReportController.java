package com.example.controller.user;

import com.example.dto.report.AppealCreateDTO;
import com.example.dto.report.ReportCreateDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.report.ReportService;
import com.example.vo.report.ReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-举报", description = "提交举报、查看举报、提交申诉")
@RestController
@RequestMapping("/user/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "提交举报")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid ReportCreateDTO dto) {
        return Result.success(reportService.createReport(dto));
    }

    @Operation(summary = "我的举报")
    @GetMapping
    public Result<PageResult> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reportService.listMyReports(pageNum, pageSize));
    }

    @Operation(summary = "举报详情")
    @GetMapping("/{id}")
    public Result<ReportVO> detail(
            @Parameter(description = "举报ID") @PathVariable Long id) {
        return Result.success(reportService.getReportDetail(id));
    }

    @Operation(summary = "提交申诉")
    @PostMapping("/{id}/appeal")
    public Result<Long> appeal(
            @Parameter(description = "举报ID") @PathVariable Long id,
            @RequestBody @Valid AppealCreateDTO dto) {
        return Result.success(reportService.createAppeal(id, dto));
    }
}