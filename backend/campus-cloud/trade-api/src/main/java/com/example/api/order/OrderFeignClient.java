package com.example.api.order;

import com.example.entity.Order;
import com.example.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 订单成交中心对外只读契约（唯一写方 = trade-service）。
 */
@FeignClient(name = "trade-service", path = "/internal/order")
public interface OrderFeignClient {

    /** 按 ID 查订单 */
    @GetMapping("/getById/{id}")
    Result<Order> getById(@PathVariable("id") Long id);

    /** 标记订单「已支付」（支付成功回调） */
    @PutMapping("/markPaid/{id}")
    Result<Void> markPaid(@PathVariable("id") Long id);
}
