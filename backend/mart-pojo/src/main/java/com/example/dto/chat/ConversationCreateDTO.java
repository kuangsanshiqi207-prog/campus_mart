package com.example.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "创建会话入参")
public class ConversationCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "对方用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "对方用户ID不能为空")
    private Long targetUserId;

    @Schema(description = "关联商品ID（可选）")
    private Long productId;
}