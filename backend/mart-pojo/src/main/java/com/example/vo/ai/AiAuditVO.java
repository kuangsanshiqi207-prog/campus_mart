package com.example.vo.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 审核结果")
public class AiAuditVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否通过审核")
    private Boolean passed;

    @Schema(description = "判定理由")
    private String reason;

    @Schema(description = "风险类别，如：违禁品/色情低俗/虚假信息/联系方式引流/辱骂攻击/身份存疑/正常")
    @JsonProperty("risk_category")
    private String riskCategory;

    @Schema(description = "风险等级 low/medium/high")
    @JsonProperty("risk_level")
    private String riskLevel;

    @Schema(description = "建议处置，如：通过/驳回/人工复核")
    @JsonProperty("suggested_action")
    private String suggestedAction;
}
