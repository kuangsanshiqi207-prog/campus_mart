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
public class AiAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String auditType;

    private Long bizId;

    private Boolean passed;

    private String reason;

    private String riskCategory;

    private String riskLevel;

    private String suggestedAction;

    private LocalDateTime createTime;
}
