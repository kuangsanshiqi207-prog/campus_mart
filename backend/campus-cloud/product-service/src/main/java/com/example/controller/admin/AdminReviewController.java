package com.example.controller.admin;

import com.example.dto.reviewManage.AdminReviewQueryDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.reviewManage.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-评价管理", description = "评价列表")
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
}
