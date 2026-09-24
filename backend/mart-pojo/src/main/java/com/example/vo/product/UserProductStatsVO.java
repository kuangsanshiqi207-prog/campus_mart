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
@Schema(description = "我的商品统计")
public class UserProductStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品总数")
    private Integer total;

    @Schema(description = "在售")
    private Integer onSale;

    @Schema(description = "已预订")
    private Integer reserved;

    @Schema(description = "已售出")
    private Integer sold;

    @Schema(description = "已下架")
    private Integer offline;

    @Schema(description = "待审核")
    private Integer pending;

    @Schema(description = "审核未通过")
    private Integer rejected;

    @Schema(description = "总浏览量")
    private Long viewTotal;

    @Schema(description = "总收藏量")
    private Long favoriteTotal;
}