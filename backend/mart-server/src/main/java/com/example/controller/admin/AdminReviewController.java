package com.example.controller.admin;

import com.example.dto.reviewManage.AdminReviewQueryDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.reviewManage.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-评价管理", description = "评价列表、删除违规评价")
@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @Operation(summary = "评价列表")
    @GetMapping
    public Result<PageResult> list(AdminReviewQueryDTO query) {
        return Result.success(adminReviewService.listReviews(query));
    }

    @Operation(summary = "删除违规评价")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "评价ID") @PathVariable Long id) {
        adminReviewService.deleteReview(id);
        return Result.success();
    }
}