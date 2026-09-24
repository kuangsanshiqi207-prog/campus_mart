package com.example.controller.admin;

import com.example.dto.userManage.AdminUserQueryDTO;
import com.example.dto.userManage.CertificationAuditDTO;
import com.example.dto.userManage.CreditAdjustDTO;
import com.example.dto.userManage.UserStatusDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.userManage.AdminUserService;
import com.example.vo.userManage.UserDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-用户管理", description = "用户列表、封禁、信用调整、认证审核")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // ==================== 用户管理 ====================

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<PageResult> listUsers(AdminUserQueryDTO query) {
        return Result.success(adminUserService.listUsers(query));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/users/{id}")
    public Result<UserDetailVO> getUserDetail(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    @Operation(summary = "封禁/解封用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody @Valid UserStatusDTO dto) {
        adminUserService.updateUserStatus(id, dto);
        return Result.success();
    }

    @Operation(summary = "调整信用分")
    @PutMapping("/users/{id}/credit")
    public Result<Void> adjustCredit(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody @Valid CreditAdjustDTO dto) {
        adminUserService.adjustCredit(id, dto);
        return Result.success();
    }

    // ==================== 认证审核 ====================

    @Operation(summary = "认证审核列表")
    @GetMapping("/certifications")
    public Result<PageResult> listCertifications(
            @Parameter(description = "状态 pending/approved/rejected") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminUserService.listCertifications(status, pageNum, pageSize));
    }

    @Operation(summary = "审核认证")
    @PutMapping("/certifications/{id}")
    public Result<Void> auditCertification(
            @Parameter(description = "认证记录ID") @PathVariable Long id,
            @RequestBody @Valid CertificationAuditDTO dto) {
        adminUserService.auditCertification(id, dto);
        return Result.success();
    }
}