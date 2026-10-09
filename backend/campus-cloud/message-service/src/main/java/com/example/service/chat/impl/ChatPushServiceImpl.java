package com.example.service.chat.impl;

import com.example.constant.ChatConstant;
import com.example.service.chat.ChatPushService;
import com.example.vo.chat.ChatMessageVO;
import com.example.websocket.ChatWebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatPushServiceImpl implements ChatPushService {

    private final ChatWebSocketServer webSocketServer;

    @Override
    public void pushNewMessage(Long toUserId, ChatMessageVO message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", ChatConstant.WS_TYPE_CHAT);
        payload.put("data", message);
        webSocketServer.pushToUser(toUserId, payload);
    }

    @Override
    public void pushReadReceipt(Long fromUserId, Long conversationId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", ChatConstant.WS_TYPE_READ);
        payload.put("conversationId", conversationId);
        webSocketServer.pushToUser(fromUserId, payload);
    }
}