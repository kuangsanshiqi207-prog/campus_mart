package com.example.dto.adminProduct;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "商品审核入参")
public class ProductAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "审核结果 pass/reject",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "审核结果不能为空")
    private String result;

    @Schema(description = "拒绝原因（reject 时必填）")
    private String reason;
}