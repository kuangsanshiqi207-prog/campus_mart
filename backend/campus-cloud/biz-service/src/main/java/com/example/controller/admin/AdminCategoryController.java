package com.example.controller.admin;

import com.example.dto.adminProduct.CategoryDTO;
import com.example.result.Result;
import com.example.service.adminProduct.AdminCategoryService;
import com.example.vo.product.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-分类管理", description = "分类增删改查")
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @Operation(summary = "分类列表")
    @GetMapping
    public Result<List<CategoryVO>> list() {
        return Result.success(adminCategoryService.listCategories());
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid CategoryDTO dto) {
        return Result.success(adminCategoryService.createCategory(dto));
    }

    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @RequestBody @Valid CategoryDTO dto) {
        adminCategoryService.updateCategory(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return Result.success();
    }
}