package com.example.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "申请交易入参")
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "买家留言")
    @Size(max = 500, message = "留言最长 500 字符")
    private String remark;

    @Schema(description = "期望交易地点")
    @Size(max = 100, message = "交易地点最长 100 字符")
    private String tradePlace;

    @Schema(description = "期望交易时间")
    private LocalDateTime appointmentTime;
}