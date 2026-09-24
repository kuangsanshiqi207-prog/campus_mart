package com.example.controller.admin;

import com.example.dto.adminProduct.AdminProductQueryDTO;
import com.example.dto.adminProduct.OfflineDTO;
import com.example.dto.adminProduct.ProductAuditDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.adminProduct.AdminProductService;
import com.example.vo.product.ProductDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-商品管理", description = "商品列表、审核、下架、删除")
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @Operation(summary = "商品列表")
    @GetMapping
    public Result<PageResult> list(AdminProductQueryDTO query) {
        return Result.success(adminProductService.listProducts(query));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        return Result.success(adminProductService.getProductDetail(id));
    }

    @Operation(summary = "人工审核")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @RequestBody @Valid ProductAuditDTO dto) {
        adminProductService.auditProduct(id, dto);
        return Result.success();
    }

    @Operation(summary = "强制下架")
    @PutMapping("/{id}/offline")
    public Result<Void> offline(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @RequestBody OfflineDTO dto) {
        adminProductService.forceOffline(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return Result.success();
    }
}