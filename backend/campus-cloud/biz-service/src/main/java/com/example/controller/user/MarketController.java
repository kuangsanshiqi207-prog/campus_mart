package com.example.controller.user;

import com.example.dto.product.ProductQueryDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.market.MarketService;
import com.example.vo.product.CategoryVO;
import com.example.vo.product.ProductDetailVO;
import com.example.vo.product.ProductVO;
import com.example.vo.product.SellerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-市集", description = "商品浏览、搜索、收藏、历史")
@RestController
@RequestMapping("/user/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    // ==================== 商品浏览 ====================

    @Operation(summary = "商品列表/搜索")
    @GetMapping("/products")
    public Result<PageResult> listProducts(ProductQueryDTO query) {
        return Result.success(marketService.listProducts(query));
    }

    @Operation(summary = "首页推荐")
    @GetMapping("/products/recommend")
    public Result<PageResult> recommend(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(marketService.listRecommend(pageNum, pageSize));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/products/{id}")
    public Result<ProductDetailVO> detail(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        return Result.success(marketService.getProductDetail(id));
    }

    @Operation(summary = "相似商品")
    @GetMapping("/products/{id}/similar")
    public Result<List<ProductVO>> similar(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "数量") @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(marketService.listSimilar(id, limit));
    }

    @Operation(summary = "查看卖家主页")
    @GetMapping("/products/{id}/seller")
    public Result<SellerVO> seller(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        return Result.success(marketService.getSeller(id));
    }

    @Operation(summary = "分类列表")
    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(marketService.listCategories());
    }

    // ==================== 收藏 ====================

    @Operation(summary = "收藏商品")
    @PostMapping("/favorites/{productId}")
    public Result<Void> addFavorite(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        marketService.addFavorite(productId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/favorites/{productId}")
    public Result<Void> removeFavorite(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        marketService.removeFavorite(productId);
        return Result.success();
    }

    @Operation(summary = "我的收藏")
    @GetMapping("/favorites")
    public Result<PageResult> favorites(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(marketService.listFavorites(pageNum, pageSize));
    }

    // ==================== 浏览历史 ====================

    @Operation(summary = "浏览历史")
    @GetMapping("/history")
    public Result<PageResult> history(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(marketService.listHistory(pageNum, pageSize));
    }

    @Operation(summary = "清空浏览历史")
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        marketService.clearHistory();
        return Result.success();
    }
}