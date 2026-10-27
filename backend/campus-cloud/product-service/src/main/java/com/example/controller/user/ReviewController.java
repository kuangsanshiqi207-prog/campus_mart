package com.example.controller.user;

import com.example.dto.review.ReviewCreateDTO;
import com.example.dto.review.ReviewReplyDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.review.ReviewService;
import com.example.vo.review.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-评价", description = "发表评价、回复评价")
@RestController
@RequestMapping("/user/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "发表评价")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid ReviewCreateDTO dto) {
        return Result.success(reviewService.createReview(dto));
    }

    @Operation(summary = "我发出的评价")
    @GetMapping("/mine")
    public Result<PageResult> mine(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.listMyReviews(pageNum, pageSize));
    }

    @Operation(summary = "我收到的评价")
    @GetMapping("/received")
    public Result<PageResult> received(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.listReceivedReviews(pageNum, pageSize));
    }

    @Operation(summary = "商品评价列表")
    @GetMapping("/product/{productId}")
    public Result<PageResult> byProduct(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.listByProduct(productId, pageNum, pageSize));
    }

    @Operation(summary = "评价详情")
    @GetMapping("/detail/{id}")
    public Result<ReviewVO> detail(
            @Parameter(description = "评价ID") @PathVariable Long id) {
        return Result.success(reviewService.getReviewDetail(id));
    }

    @Operation(summary = "回复评价")
    @PostMapping("/{id}/reply")
    public Result<Void> reply(
            @Parameter(description = "评价ID") @PathVariable Long id,
            @RequestBody @Valid ReviewReplyDTO dto) {
        reviewService.replyReview(id, dto);
        return Result.success();
    }
}