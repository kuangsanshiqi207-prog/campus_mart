package com.example.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "发布/修改商品入参")
public class ProductCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长 100 字符")
    private String title;

    @Schema(description = "描述")
    @Size(max = 2000, message = "描述最长 2000 字符")
    private String description;

    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    @Schema(description = "原价")
    @DecimalMin(value = "0", message = "原价不能为负")
    private BigDecimal originalPrice;

    @Schema(description = "成色 new/almost_new/good/normal",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "成色不能为空")
    private String quality;

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Schema(description = "交易方式 face/express/self_pickup",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "交易方式不能为空")
    private String tradeType;

    @Schema(description = "交易地点")
    @Size(max = 100, message = "交易地点最长 100 字符")
    private String tradePlace;

    @Schema(description = "图片文件ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "至少上传一张图片")
    @Size(max = 9, message = "最多上传 9 张图片")
    private List<Long> imageFileIds;
}