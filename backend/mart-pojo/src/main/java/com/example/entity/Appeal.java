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
public class Appeal implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long reportId;

    private Long userId;

    private String reason;

    private String images;

    private String status;

    private String handleReason;

    private Long handlerId;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    private Integer deleted;
}