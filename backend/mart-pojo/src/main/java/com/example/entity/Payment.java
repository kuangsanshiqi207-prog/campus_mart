package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String paymentNo;

    private Long orderId;

    private String orderNo;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal amount;

    private String status;

    private String channel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime paidTime;
}
