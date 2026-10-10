package com.example.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "AI 审核入参")
public class AiAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "审核类型 product/review/report/certification",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("audit_type")
    private String auditType;

    @Schema(description = "文本字段，键对齐实体字段名")
    @JsonProperty("text_fields")
    private Map<String, String> textFields = new HashMap<>();

    @Schema(description = "图片 URL 或 base64 data URL")
    @JsonProperty("image_urls")
    private List<String> imageUrls = new ArrayList<>();

    @Schema(description = "其他附加信息")
    private Map<String, Object> extra = new HashMap<>();
}
