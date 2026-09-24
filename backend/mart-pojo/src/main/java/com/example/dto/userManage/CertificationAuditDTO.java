package com.example.dto.userManage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "认证审核入参")
public class CertificationAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "审核结果 approved/rejected",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "审核结果不能为空")
    private String status;

    @Schema(description = "拒绝原因（rejected 时必填）")
    private String reason;
}