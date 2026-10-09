package com.example.controller.user;

import com.example.dto.user.AddressDTO;
import com.example.dto.user.CertificationDTO;
import com.example.dto.user.PasswordUpdateDTO;
import com.example.dto.user.UserUpdateDTO;
import com.example.result.Result;
import com.example.service.user.UserProfileService;
import com.example.vo.user.AddressVO;
import com.example.vo.user.CertificationVO;
import com.example.vo.user.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-个人中心", description = "个人资料、修改密码、校园认证、地址管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    // ==================== 个人资料 ====================

    @Operation(summary = "获取当前用户资料")
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        return Result.success(userProfileService.getProfile());
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UserUpdateDTO dto) {
        userProfileService.updateProfile(dto);
        return Result.success();
    }

    @Operation(summary = "修改密码", description = "修改成功后当前 token 失效，需要重新登录")
    @PutMapping("/profile/password")
    public Result<Void> updatePassword(
            @RequestBody @Valid PasswordUpdateDTO dto,
            @Parameter(description = "Bearer token")
            @RequestHeader(value = "Authorization", required = false) String token) {
        userProfileService.updatePassword(dto, token);
        return Result.success();
    }

    // ==================== 校园认证 ====================

    @Operation(summary = "提交校园认证")
    @PostMapping("/certification")
    public Result<Long> submitCertification(@RequestBody @Valid CertificationDTO dto) {
        return Result.success(userProfileService.submitCertification(dto));
    }

    @Operation(summary = "查看认证状态")
    @GetMapping("/certification")
    public Result<CertificationVO> getCertification() {
        return Result.success(userProfileService.getCertification());
    }

    // ==================== 地址管理 ====================

    @Operation(summary = "地址列表")
    @GetMapping("/addresses")
    public Result<List<AddressVO>> listAddresses() {
        return Result.success(userProfileService.listAddresses());
    }

    @Operation(summary = "新增地址")
    @PostMapping("/addresses")
    public Result<Long> addAddress(@RequestBody @Valid AddressDTO dto) {
        return Result.success(userProfileService.addAddress(dto));
    }

    @Operation(summary = "修改地址")
    @PutMapping("/addresses/{id}")
    public Result<Void> updateAddress(
            @Parameter(description = "地址ID") @PathVariable Long id,
            @RequestBody @Valid AddressDTO dto) {
        userProfileService.updateAddress(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/addresses/{id}")
    public Result<Void> deleteAddress(
            @Parameter(description = "地址ID") @PathVariable Long id) {
        userProfileService.deleteAddress(id);
        return Result.success();
    }
}