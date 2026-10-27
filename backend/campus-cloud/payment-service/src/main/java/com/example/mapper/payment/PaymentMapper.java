package com.example.mapper.payment;

import com.example.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PaymentMapper {

    void insert(Payment payment);

    Payment getById(Long id);

    Payment getByOrderId(Long orderId);

    void markPaid(@Param("id") Long id,
                  @Param("paidTime") LocalDateTime paidTime);
}
