package com.example.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "商品查询入参")
public class ProductQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词", example = "自行车")
    private String keyword;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "最低价")
    private BigDecimal minPrice;

    @Schema(description = "最高价")
    private BigDecimal maxPrice;

    @Schema(description = "成色 new/almost_new/good/normal")
    private String quality;

    @Schema(description = "排序 latest/price_asc/price_desc/hot", example = "latest")
    private String sort;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}