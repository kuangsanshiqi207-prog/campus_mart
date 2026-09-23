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
@Schema(description = "卖家信息")
public class SellerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "卖家ID")
    private Long id;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "信用分")
    private Integer creditScore;

    @Schema(description = "是否校园认证 0否 1是")
    private Integer certified;

    @Schema(description = "在售商品数")
    private Integer productCount;
}