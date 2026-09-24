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
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long reporterId;

    private String targetType;

    private Long targetId;

    private String reason;

    private String description;

    private String images;

    private String status;

    private String action;

    private String handleReason;

    private Long handlerId;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    private Integer deleted;
}