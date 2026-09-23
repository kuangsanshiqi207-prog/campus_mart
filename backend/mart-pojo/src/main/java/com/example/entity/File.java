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
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String url;

    private String originalName;

    private Long fileSize;

    private String contentType;

    private String status;

    private LocalDateTime createTime;

    private Integer deleted;
}