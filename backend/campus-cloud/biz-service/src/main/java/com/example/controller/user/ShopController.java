package com.example.controller.user;

import com.example.dto.product.MyProductQueryDTO;
import com.example.dto.product.ProductCreateDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.shop.ShopService;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.UserProductStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-我的商店", description = "商品发布、编辑、上下架、数据")
@RestController
@RequestMapping("/user/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "发布商品")
    @PostMapping("/products")
    public Result<Long> create(@RequestBody @Valid ProductCreateDTO dto) {
        return Result.success(shopService.createProduct(dto));
    }

    @Operation(summary = "修改商品")
    @PutMapping("/products/{id}")
    public Result<Void> update(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @RequestBody @Valid ProductCreateDTO dto) {
        shopService.updateProduct(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/products/{id}")
    public Result<Void> delete(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        shopService.deleteProduct(id);
        return Result.success();
    }

    @Operation(summary = "下架商品")
    @PutMapping("/products/{id}/offline")
    public Result<Void> offline(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        shopService.offlineProduct(id);
        return Result.success();
    }

    @Operation(summary = "重新上架")
    @PutMapping("/products/{id}/online")
    public Result<Void> online(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        shopService.onlineProduct(id);
        return Result.success();
    }

    @Operation(summary = "我的商品列表")
    @GetMapping("/products")
    public Result<PageResult> list(MyProductQueryDTO query) {
        return Result.success(shopService.listMyProducts(query));
    }

    @Operation(summary = "我的商品详情")
    @GetMapping("/products/{id}")
    public Result<ProductDetailVO> detail(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        return Result.success(shopService.getMyProductDetail(id));
    }

    @Operation(summary = "我的商品统计")
    @GetMapping("/stats")
    public Result<UserProductStatsVO> stats() {
        return Result.success(shopService.getMyStats());
    }
}