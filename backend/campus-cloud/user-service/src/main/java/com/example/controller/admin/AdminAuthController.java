package com.example.controller.admin;

import com.example.dto.admin.AdminLoginDTO;
import com.example.result.Result;
import com.example.service.admin.AdminService;
import com.example.vo.admin.AdminVO;
import com.example.vo.user.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-认证", description = "管理员登录、登出、当前信息")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid AdminLoginDTO dto) {
        return Result.success(adminService.login(dto));
    }

    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public Result<Void> logout(
            @Parameter(description = "Bearer token")
            @RequestHeader(value = "Authorization", required = false) String token) {
        adminService.logout(token);
        return Result.success();
    }

    @Operation(summary = "当前管理员信息")
    @GetMapping("/profile")
    public Result<AdminVO> profile() {
        return Result.success(adminService.getCurrentAdmin());
    }
}