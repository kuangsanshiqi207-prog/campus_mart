package com.example.service.order.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.constant.OrderConstant;
import com.example.constant.ProductConstant;
import com.example.context.BaseContext;
import com.example.dto.order.OrderCreateDTO;
import com.example.dto.order.OrderQueryDTO;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.exception.BaseException;
import com.example.mapper.order.OrderMapper;
import com.example.mapper.product.ProductMapper;
import com.example.result.PageResult;
import com.example.service.message.MessageSender;
import com.example.service.order.OrderService;
import com.example.vo.order.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final MessageSender messageSender;

    // ==================== 申请交易 ====================

    @Override
    @Transactional
    public Long createOrder(OrderCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 1. 查商品
        Product product = productMapper.getById(dto.getProductId());
        if (product == null) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        if (!ProductConstant.AUDIT_APPROVED.equals(product.getAuditStatus())) {
            throw new BaseException(MessageConstant.PRODUCT_NOT_FOUND);
        }
        if (!ProductConstant.STATUS_ON_SALE.equals(product.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_PRODUCT_NOT_AVAILABLE);
        }

        // 2. 不能买自己的商品
        if (product.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.ORDER_CANNOT_BUY_OWN);
        }

        // 3. 检查是否已有待处理订单
        Order existing = orderMapper.getPendingByProductId(dto.getProductId());
        if (existing != null) {
            throw new BaseException(MessageConstant.ORDER_ALREADY_EXISTS);
        }

        // 4. 生成订单
        Long orderId = IdUtil.getSnowflakeNextId();
        String orderNo = generateOrderNo();

        Order order = Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .productId(dto.getProductId())
                .buyerId(userId)
                .sellerId(product.getSellerId())
                .amount(product.getPrice())
                .status(OrderConstant.STATUS_PENDING)
                .tradeType(product.getTradeType())
                .tradePlace(dto.getTradePlace() != null ? dto.getTradePlace() : product.getTradePlace())
                .appointmentTime(dto.getAppointmentTime())
                .remark(dto.getRemark())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        orderMapper.insert(order);

        // 5. 通知卖家
        messageSender.send(
                product.getSellerId(),
                NotificationConstant.TYPE_ORDER,
                NotificationConstant.ORDER_CREATED_TITLE,
                String.format(NotificationConstant.ORDER_CREATED_CONTENT, product.getTitle()),
                orderId
        );

        return orderId;
    }

    // ==================== 卖家接受 ====================

    @Override
    @Transactional
    public void acceptOrder(Long id) {
        Long userId = BaseContext.getCurrentId();

        Order order = getOrderOrThrow(id);
        if (!order.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.ORDER_SELLER_NOT_MATCH);
        }
        if (!OrderConstant.STATUS_PENDING.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orderMapper.updateStatus(id, OrderConstant.STATUS_ACCEPTED);
        productMapper.updateStatus(order.getProductId(), ProductConstant.STATUS_RESERVED);

        // 通知买家
        Product product = productMapper.getById(order.getProductId());
        String productTitle = product != null ? product.getTitle() : "";
        messageSender.send(
                order.getBuyerId(),
                NotificationConstant.TYPE_ORDER,
                NotificationConstant.ORDER_ACCEPTED_TITLE,
                String.format(NotificationConstant.ORDER_ACCEPTED_CONTENT, productTitle),
                id
        );
    }

    // ==================== 卖家拒绝 ====================

    @Override
    @Transactional
    public void rejectOrder(Long id, String reason) {
        Long userId = BaseContext.getCurrentId();

        Order order = getOrderOrThrow(id);
        if (!order.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.ORDER_SELLER_NOT_MATCH);
        }
        if (!OrderConstant.STATUS_PENDING.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orderMapper.updateRejectReason(id, OrderConstant.STATUS_REJECTED, reason);

        // 通知买家
        Product product = productMapper.getById(order.getProductId());
        String productTitle = product != null ? product.getTitle() : "";
        messageSender.send(
                order.getBuyerId(),
                NotificationConstant.TYPE_ORDER,
                NotificationConstant.ORDER_REJECTED_TITLE,
                String.format(NotificationConstant.ORDER_REJECTED_CONTENT, productTitle, reason),
                id
        );
    }

    // ==================== 取消订单 ====================

    @Override
    @Transactional
    public void cancelOrder(Long id, String reason) {
        Long userId = BaseContext.getCurrentId();

        Order order = getOrderOrThrow(id);
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.ORDER_NO_PERMISSION);
        }
        if (!OrderConstant.STATUS_PENDING.equals(order.getStatus())
                && !OrderConstant.STATUS_ACCEPTED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orderMapper.updateCancelReason(id, OrderConstant.STATUS_CANCELLED, reason);
        productMapper.updateStatus(order.getProductId(), ProductConstant.STATUS_ON_SALE);

        // 通知对方
        Long notifyUserId = order.getBuyerId().equals(userId)
                ? order.getSellerId()
                : order.getBuyerId();
        messageSender.send(
                notifyUserId,
                NotificationConstant.TYPE_ORDER,
                NotificationConstant.ORDER_CANCELLED_TITLE,
                String.format(NotificationConstant.ORDER_CANCELLED_CONTENT, order.getOrderNo(), reason),
                id
        );
    }

    // ==================== 买家确认完成 ====================

    @Override
    @Transactional
    public void completeOrder(Long id) {
        Long userId = BaseContext.getCurrentId();

        Order order = getOrderOrThrow(id);
        if (!order.getBuyerId().equals(userId)) {
            throw new BaseException(MessageConstant.ORDER_BUYER_NOT_MATCH);
        }
        if (!OrderConstant.STATUS_ACCEPTED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orderMapper.updateCompleted(id, OrderConstant.STATUS_COMPLETED);
        productMapper.updateStatus(order.getProductId(), ProductConstant.STATUS_SOLD);

        // 通知双方
        String content = String.format(NotificationConstant.ORDER_COMPLETED_CONTENT, order.getOrderNo());
        messageSender.send(
                order.getSellerId(),
                NotificationConstant.TYPE_ORDER,
                NotificationConstant.ORDER_COMPLETED_TITLE,
                content,
                id
        );
        messageSender.send(
                order.getBuyerId(),
                NotificationConstant.TYPE_ORDER,
                NotificationConstant.ORDER_COMPLETED_TITLE,
                content,
                id
        );
    }

    // ==================== 订单列表 ====================

    @Override
    public PageResult listOrders(OrderQueryDTO query) {
        Long userId = BaseContext.getCurrentId();

        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1 || query.getPageSize() > 100) {
            query.setPageSize(10);
        }

        int offset = (query.getPageNum() - 1) * query.getPageSize();
        int limit = query.getPageSize();

        boolean isBuy = OrderConstant.TYPE_BUY.equals(query.getType());
        Long total;
        List<OrderVO> list;

        if (isBuy) {
            total = orderMapper.countByBuyer(userId, query.getStatus());
            list = orderMapper.listByBuyer(userId, query.getStatus(), offset, limit);
        } else {
            total = orderMapper.countBySeller(userId, query.getStatus());
            list = orderMapper.listBySeller(userId, query.getStatus(), offset, limit);
        }

        return new PageResult(total, list);
    }

    // ==================== 订单详情 ====================

    @Override
    public OrderVO getOrderDetail(Long id) {
        Long userId = BaseContext.getCurrentId();

        Order order = getOrderOrThrow(id);
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.ORDER_NO_PERMISSION);
        }

        return orderMapper.getOrderVOById(id);
    }

    // ==================== 私有方法 ====================

    private Order getOrderOrThrow(Long id) {
        Order order = orderMapper.getById(id);
        if (order == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        return order;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = RandomUtil.randomNumbers(6);
        return timestamp + random;
    }
}