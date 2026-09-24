package com.example.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "提交举报入参")
public class ReportCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "举报对象类型 product/user/order",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "举报对象类型不能为空")
    private String targetType;

    @Schema(description = "举报对象ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "举报对象ID不能为空")
    private Long targetId;

    @Schema(description = "举报原因（短）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "举报原因不能为空")
    @Size(max = 100, message = "举报原因最长 100 字符")
    private String reason;

    @Schema(description = "详细描述")
    @Size(max = 500, message = "详细描述最长 500 字符")
    private String description;

    @Schema(description = "证据图片文件ID列表")
    @Size(max = 9, message = "最多上传 9 张图片")
    private List<Long> imageFileIds;
}