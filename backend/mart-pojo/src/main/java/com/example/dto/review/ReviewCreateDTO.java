package com.example.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "发表评价入参")
public class ReviewCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "评分 1-5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低 1 星")
    @Max(value = 5, message = "评分最高 5 星")
    private Integer score;

    @Schema(description = "评价内容")
    @Size(max = 500, message = "评价内容最长 500 字符")
    private String content;

    @Schema(description = "图片文件ID列表")
    @Size(max = 9, message = "最多上传 9 张图片")
    private List<Long> imageFileIds;
}