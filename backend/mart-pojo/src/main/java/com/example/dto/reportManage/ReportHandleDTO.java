package com.example.dto.reportManage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "处理举报入参")
public class ReportHandleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "处理结果 valid/invalid",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处理结果不能为空")
    private String result;

    @Schema(description = "处理动作 none/offline/ban/warn")
    private String action;

    @Schema(description = "处理说明")
    private String reason;
}