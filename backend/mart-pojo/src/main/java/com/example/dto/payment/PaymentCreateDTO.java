package com.example.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentCreateDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
