package com.example.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 客服对话消息")
public class AiChatMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色 user/assistant/system")
    private String role = "user";

    @Schema(description = "消息内容")
    private String content;
}
