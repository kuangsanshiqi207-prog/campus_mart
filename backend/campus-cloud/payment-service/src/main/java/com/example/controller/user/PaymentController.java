package com.example.controller.user;

import com.example.dto.payment.PaymentCreateDTO;
import com.example.entity.Payment;
import com.example.result.Result;
import com.example.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-支付", description = "订单支付（模拟）")
@RestController
@RequestMapping("/user/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "创建支付单")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid PaymentCreateDTO dto) {
        return Result.success(paymentService.createPayment(dto));
    }

    @Operation(summary = "支付（模拟）")
    @PostMapping("/{id}/pay")
    public Result<Void> pay(
            @Parameter(description = "支付单ID") @PathVariable Long id) {
        paymentService.pay(id);
        return Result.success();
    }

    @Operation(summary = "支付单详情")
    @GetMapping("/{id}")
    public Result<Payment> detail(
            @Parameter(description = "支付单ID") @PathVariable Long id) {
        return Result.success(paymentService.getPayment(id));
    }
}
