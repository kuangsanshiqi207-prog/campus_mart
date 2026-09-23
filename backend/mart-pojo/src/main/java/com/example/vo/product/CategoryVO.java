package com.example.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品分类")
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名")
    private String name;

    @Schema(description = "父分类ID，0为一级分类")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;
}