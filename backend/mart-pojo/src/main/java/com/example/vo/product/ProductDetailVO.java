package com.example.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品详情")
public class ProductDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "成色")
    private String quality;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名")
    private String categoryName;

    @Schema(description = "交易方式")
    private String tradeType;

    @Schema(description = "交易地点")
    private String tradePlace;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "图片列表")
    private List<String> images;

    @Schema(description = "卖家信息")
    private SellerVO seller;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "收藏数")
    private Integer favoriteCount;

    @Schema(description = "当前用户是否已收藏")
    private Boolean favorited;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;
}