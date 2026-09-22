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
public class UserCertification implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String school;

    private String studentNo;

    private String realName;

    private String cardImage;

    private String status;

    private String reason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}