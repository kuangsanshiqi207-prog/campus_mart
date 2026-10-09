package com.example.service.chat;

import com.example.vo.chat.ChatMessageVO;

/**
 * 消息推送服务，供 ChatService 调用
 */
public interface ChatPushService {

    void pushNewMessage(Long toUserId, ChatMessageVO message);

    void pushReadReceipt(Long fromUserId, Long conversationId);
}