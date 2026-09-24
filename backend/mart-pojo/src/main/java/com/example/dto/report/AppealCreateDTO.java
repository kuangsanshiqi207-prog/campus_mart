package com.example.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "提交申诉入参")
public class AppealCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "申诉理由", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "申诉理由不能为空")
    @Size(max = 500, message = "申诉理由最长 500 字符")
    private String reason;

    @Schema(description = "证据图片文件ID列表")
    @Size(max = 9, message = "最多上传 9 张图片")
    private List<Long> imageFileIds;
}