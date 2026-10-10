package com.example.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "AI 客服对话入参")
public class AiChatDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话 ID，用于隔离历史")
    @JsonProperty("session_id")
    private String sessionId = "default";

    @Schema(description = "用户最新消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "可选：首次调用时可传入客户端历史")
    private List<AiChatMessageDTO> history = new ArrayList<>();
}
