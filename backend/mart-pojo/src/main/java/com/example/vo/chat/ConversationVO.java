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
@Schema(description = "会话信息")
public class ConversationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "对方用户ID")
    private Long targetUserId;

    @Schema(description = "对方昵称")
    private String targetNickname;

    @Schema(description = "对方头像")
    private String targetAvatar;

    @Schema(description = "最后一条消息")
    private String lastMessage;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageAt;

    @Schema(description = "未读数")
    private Integer unread;
}