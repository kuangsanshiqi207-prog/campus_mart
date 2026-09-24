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
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long productId;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal amount;

    private String status;

    private String tradeType;

    private String tradePlace;

    private LocalDateTime appointmentTime;

    private String remark;

    private String rejectReason;

    private String cancelReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime completedTime;

    private Integer deleted;
}