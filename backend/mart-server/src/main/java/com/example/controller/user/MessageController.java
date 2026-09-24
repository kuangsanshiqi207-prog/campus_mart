package com.example.controller.user;

import com.example.result.PageResult;
import com.example.result.Result;
import com.example.service.message.MessageService;
import com.example.vo.message.AnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户端-消息", description = "站内信、公告")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ==================== 消息 ====================

    @Operation(summary = "消息列表")
    @GetMapping("/messages")
    public Result<PageResult> listMessages(
            @Parameter(description = "类型 order/audit/report/system") @RequestParam(required = false) String type,
            @Parameter(description = "是否已读 0未读 1已读") @RequestParam(required = false) Integer isRead,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(messageService.listMessages(type, isRead, pageNum, pageSize));
    }

    @Operation(summary = "未读消息数")
    @GetMapping("/messages/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.getUnreadCount());
    }

    @Operation(summary = "标记已读")
    @PutMapping("/messages/{id}/read")
    public Result<Void> markRead(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        messageService.markRead(id);
        return Result.success();
    }

    @Operation(summary = "全部已读")
    @PutMapping("/messages/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead();
        return Result.success();
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/messages/{id}")
    public Result<Void> delete(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        messageService.deleteMessage(id);
        return Result.success();
    }

    // ==================== 公告 ====================

    @Operation(summary = "公告列表")
    @GetMapping("/announcements")
    public Result<PageResult> listAnnouncements(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(messageService.listAnnouncements(pageNum, pageSize));
    }

    @Operation(summary = "公告详情")
    @GetMapping("/announcements/{id}")
    public Result<AnnouncementVO> getAnnouncement(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        return Result.success(messageService.getAnnouncement(id));
    }
}