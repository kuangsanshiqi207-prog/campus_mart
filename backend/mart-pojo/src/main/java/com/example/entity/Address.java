package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String receiver;

    private String phone;

    private String region;

    private String detail;

    private Integer isDefault;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}