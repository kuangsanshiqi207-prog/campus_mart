package com.example.dto.adminProduct;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "分类入参")
public class CategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名不能为空")
    private String name;

    @Schema(description = "父分类ID，0为一级分类")
    private Long parentId = 0L;

    @Schema(description = "排序")
    private Integer sort = 0;

    @Schema(description = "状态 1启用 0禁用")
    private Integer status = 1;
}