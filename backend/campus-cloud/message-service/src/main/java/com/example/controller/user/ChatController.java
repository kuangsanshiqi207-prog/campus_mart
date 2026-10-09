package com.example.controller.user;

import com.example.dto.chat.ChatSendDTO;
import com.example.dto.chat.ConversationCreateDTO;
import com.example.result.Result;
import com.example.service.chat.ChatService;
import com.example.vo.chat.ChatMessageVO;
import com.example.vo.chat.ConversationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-私信", description = "会话、消息")
@RestController
@RequestMapping("/user/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "创建/获取会话")
    @PostMapping("/conversations")
    public Result<ConversationVO> createOrGet(@RequestBody @Valid ConversationCreateDTO dto) {
        return Result.success(chatService.createOrGetConversation(dto));
    }

    @Operation(summary = "会话列表")
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> listConversations() {
        return Result.success(chatService.listConversations());
    }

    @Operation(summary = "历史消息（游标分页）")
    @GetMapping("/conversations/{id}/messages")
    public Result<List<ChatMessageVO>> listMessages(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @Parameter(description = "游标，上一条消息ID") @RequestParam(required = false) Long lastId,
            @Parameter(description = "数量") @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(chatService.listMessages(id, lastId, limit));
    }

    @Operation(summary = "标记会话已读")
    @PutMapping("/conversations/{id}/read")
    public Result<Void> markRead(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        chatService.markRead(id);
        return Result.success();
    }

    @Operation(summary = "发送消息（HTTP 备用）")
    @PostMapping("/conversations/{id}/messages")
    public Result<ChatMessageVO> send(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @RequestBody @Valid ChatSendDTO dto) {
        Long userId = com.example.context.BaseContext.getCurrentId();
        return Result.success(chatService.sendMessage(id, userId, dto));
    }
}