package com.example.controller.user;

import com.example.dto.order.OrderCreateDTO;
import com.example.dto.order.OrderQueryDTO;
import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.order.OrderService;
import com.example.vo.order.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户端-交易", description = "申请交易、订单管理")
@RestController
@RequestMapping("/user/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "申请交易")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid OrderCreateDTO dto) {
        return Result.success(orderService.createOrder(dto));
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public Result<PageResult> list(OrderQueryDTO query) {
        return Result.success(orderService.listOrders(query));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }

    @Operation(summary = "卖家接受交易")
    @PutMapping("/{id}/accept")
    public Result<Void> accept(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        orderService.acceptOrder(id);
        return Result.success();
    }

    @Operation(summary = "卖家拒绝交易")
    @PutMapping("/{id}/reject")
    public Result<Void> reject(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        orderService.rejectOrder(id, reason);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        orderService.cancelOrder(id, reason);
        return Result.success();
    }

    @Operation(summary = "买家确认完成")
    @PutMapping("/{id}/complete")
    public Result<Void> complete(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        orderService.completeOrder(id);
        return Result.success();
    }
}