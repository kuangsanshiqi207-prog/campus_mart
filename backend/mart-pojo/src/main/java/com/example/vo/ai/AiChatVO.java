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
@Schema(description = "AI 客服同步回复")
public class AiChatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话 ID")
    @JsonProperty("session_id")
    private String sessionId;

    @Schema(description = "助手完整回复")
    private String content;
}
