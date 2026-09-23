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
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long sellerId;

    private Long categoryId;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String quality;

    private String tradeType;

    private String tradePlace;

    private String status;

    private String auditStatus;

    private String auditReason;

    private Integer viewCount;

    private Integer favoriteCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}