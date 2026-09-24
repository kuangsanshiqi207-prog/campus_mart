package com.example.dto.userManage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "封禁/解封入参")
public class UserStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态 normal/banned", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "状态不能为空")
    private String status;

    @Schema(description = "原因")
    private String reason;
}