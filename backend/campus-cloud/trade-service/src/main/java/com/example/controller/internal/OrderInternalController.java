package com.example.controller.internal;

import com.example.api.order.OrderFeignClient;
import com.example.constant.OrderConstant;
import com.example.entity.Order;
import com.example.mapper.order.OrderMapper;
import com.example.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/order")
@RequiredArgsConstructor
public class OrderInternalController implements OrderFeignClient {

    private final OrderMapper orderMapper;

    @Override
    public Result<Order> getById(Long id) {
        Order order = orderMapper.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @Override
    public Result<Void> markPaid(Long id) {
        Order order = orderMapper.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (OrderConstant.STATUS_PAID.equals(order.getStatus())) {
            return Result.success();
        }
        if (!OrderConstant.STATUS_ACCEPTED.equals(order.getStatus())) {
            return Result.error("订单状态错误");
        }
        orderMapper.updateStatus(id, OrderConstant.STATUS_PAID);
        return Result.success();
    }
}
