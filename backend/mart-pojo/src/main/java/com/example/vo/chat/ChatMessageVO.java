package com.example.vo.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息")
public class ChatMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "发送者ID")
    private Long fromUserId;

    @Schema(description = "接收者ID")
    private Long toUserId;

    @Schema(description = "消息类型")
    private String type;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "状态 sent/read")
    private String status;

    @Schema(description = "发送时间")
    private LocalDateTime createTime;
}