package com.example.service.payment.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.api.message.MessageFeignClient;
import com.example.api.order.OrderFeignClient;
import com.example.constant.MessageConstant;
import com.example.constant.NotificationConstant;
import com.example.constant.OrderConstant;
import com.example.constant.PaymentConstant;
import com.example.context.BaseContext;
import com.example.dto.payment.PaymentCreateDTO;
import com.example.entity.Order;
import com.example.entity.Payment;
import com.example.exception.BaseException;
import com.example.mapper.payment.PaymentMapper;
import com.example.result.Result;
import com.example.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderFeignClient orderFeignClient;
    private final MessageFeignClient messageFeignClient;

    @Override
    @Transactional
    public Long createPayment(PaymentCreateDTO dto) {
        Long buyerId = BaseContext.getCurrentId();

        Order order = getOrder(dto.getOrderId());
        if (order == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BaseException(MessageConstant.ORDER_NO_PERMISSION);
        }
        if (!OrderConstant.STATUS_ACCEPTED.equals(order.getStatus())) {
            throw new BaseException(MessageConstant.PAYMENT_ORDER_NOT_ACCEPTED);
        }

        Payment existing = paymentMapper.getByOrderId(dto.getOrderId());
        if (existing != null) {
            if (PaymentConstant.STATUS_PAID.equals(existing.getStatus())) {
                throw new BaseException(MessageConstant.PAYMENT_ALREADY_PAID);
            }
            return existing.getId();
        }

        Long id = IdUtil.getSnowflakeNextId();
        Payment payment = Payment.builder()
                .id(id)
                .paymentNo(generatePaymentNo())
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .amount(order.getAmount())
                .status(PaymentConstant.STATUS_PENDING)
                .channel(PaymentConstant.CHANNEL_MOCK)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        paymentMapper.insert(payment);
        return id;
    }

    @Override
    @Transactional
    public void pay(Long id) {
        Long buyerId = BaseContext.getCurrentId();

        Payment payment = getPaymentOrThrow(id);
        if (!payment.getBuyerId().equals(buyerId)) {
            throw new BaseException(MessageConstant.ORDER_NO_PERMISSION);
        }
        if (PaymentConstant.STATUS_PAID.equals(payment.getStatus())) {
            return;
        }
        if (!PaymentConstant.STATUS_PENDING.equals(payment.getStatus())) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 模拟支付：直接置为已支付
        paymentMapper.markPaid(id, LocalDateTime.now());

        // 回调订单中心：标记订单已支付
        Result<Void> r = orderFeignClient.markPaid(payment.getOrderId());
        if (r == null || r.getCode() != 1) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 通知卖家
        messageFeignClient.send(
                payment.getSellerId(),
                NotificationConstant.TYPE_ORDER,
                NotificationConstant.ORDER_PAID_TITLE,
                String.format(NotificationConstant.ORDER_PAID_CONTENT, payment.getOrderNo()),
                payment.getOrderId()
        );
    }

    @Override
    public Payment getPayment(Long id) {
        Long userId = BaseContext.getCurrentId();
        Payment payment = getPaymentOrThrow(id);
        if (!payment.getBuyerId().equals(userId) && !payment.getSellerId().equals(userId)) {
            throw new BaseException(MessageConstant.ORDER_NO_PERMISSION);
        }
        return payment;
    }

    private Payment getPaymentOrThrow(Long id) {
        Payment payment = paymentMapper.getById(id);
        if (payment == null) {
            throw new BaseException(MessageConstant.PAYMENT_NOT_FOUND);
        }
        return payment;
    }

    private Order getOrder(Long id) {
        Result<Order> r = orderFeignClient.getById(id);
        if (r == null || r.getCode() != 1 || r.getData() == null) {
            return null;
        }
        return r.getData();
    }

    private String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "P" + timestamp + RandomUtil.randomNumbers(6);
    }
}
