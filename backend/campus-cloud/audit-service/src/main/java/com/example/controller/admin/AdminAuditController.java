package com.example.controller.admin;

import com.example.dto.adminProduct.ProductAuditDTO;
import com.example.dto.userManage.CertificationAuditDTO;
import com.example.result.Result;
import com.example.service.audit.AuditService;
import com.example.vo.ai.AiAuditVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-内容审核", description = "商品审核、评论审核、校园认证审核")
@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AuditService auditService;

    @Operation(summary = "商品人工审核")
    @PutMapping("/products/{id}")
    public Result<Void> auditProduct(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @RequestBody @Valid ProductAuditDTO dto) {
        auditService.auditProduct(id, dto);
        return Result.success();
    }

    @Operation(summary = "商品 AI 辅助审核")
    @GetMapping("/products/{id}/ai-audit")
    public Result<AiAuditVO> aiAuditProduct(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        AiAuditVO vo = auditService.aiAuditProduct(id);
        return vo == null ? Result.error("AI 审核暂不可用，请人工审核") : Result.success(vo);
    }

    @Operation(summary = "删除违规评论")
    @DeleteMapping("/reviews/{id}")
    public Result<Void> deleteReview(
            @Parameter(description = "评价ID") @PathVariable Long id) {
        auditService.deleteReview(id);
        return Result.success();
    }

    @Operation(summary = "评论 AI 辅助审核")
    @GetMapping("/reviews/{id}/ai-audit")
    public Result<AiAuditVO> aiAuditReview(
            @Parameter(description = "评价ID") @PathVariable Long id) {
        AiAuditVO vo = auditService.aiAuditReview(id);
        return vo == null ? Result.error("AI 审核暂不可用，请人工审核") : Result.success(vo);
    }

    @Operation(summary = "校园认证审核")
    @PutMapping("/certifications/{id}")
    public Result<Void> auditCertification(
            @Parameter(description = "认证记录ID") @PathVariable Long id,
            @RequestBody @Valid CertificationAuditDTO dto) {
        auditService.auditCertification(id, dto);
        return Result.success();
    }

    @Operation(summary = "校园认证 AI 辅助审核")
    @GetMapping("/certifications/{id}/ai-audit")
    public Result<AiAuditVO> aiAuditCertification(
            @Parameter(description = "认证记录ID") @PathVariable Long id) {
        AiAuditVO vo = auditService.aiAuditCertification(id);
        return vo == null ? Result.error("AI 审核暂不可用，请人工审核") : Result.success(vo);
    }
}
