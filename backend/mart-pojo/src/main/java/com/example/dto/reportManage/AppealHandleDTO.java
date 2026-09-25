package com.example.dto.reportManage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "处理申诉入参")
public class AppealHandleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "处理结果 approve/reject",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处理结果不能为空")
    private String result;

    @Schema(description = "处理说明")
    private String reason;
}