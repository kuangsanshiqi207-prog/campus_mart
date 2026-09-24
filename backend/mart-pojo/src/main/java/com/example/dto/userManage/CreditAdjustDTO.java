package com.example.dto.userManage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "调整信用分入参")
public class CreditAdjustDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "变动值，正数加分，负数扣分",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变动值不能为空")
    private Integer delta;

    @Schema(description = "变动原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "原因不能为空")
    private String reason;
}