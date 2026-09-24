package com.example.service.order;

import com.example.dto.order.OrderCreateDTO;
import com.example.dto.order.OrderQueryDTO;
import com.example.result.PageResult;
import com.example.vo.order.OrderVO;

public interface OrderService {

    Long createOrder(OrderCreateDTO dto);

    void acceptOrder(Long id);

    void rejectOrder(Long id, String reason);

    void cancelOrder(Long id, String reason);

    void completeOrder(Long id);

    PageResult listOrders(OrderQueryDTO query);

    OrderVO getOrderDetail(Long id);
}