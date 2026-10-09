package com.example.websocket;

import com.example.constant.ChatConstant;
import com.example.json.JacksonObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天 WebSocket 服务端
 *
 * 职责：
 * 1. 管理在线用户的 WebSocket 连接
 * 2. 处理心跳（ping/pong）
 * 3. 向指定用户推送消息
 *
 * 不负责：
 * - 接收业务消息（发送消息走 HTTP 接口）
 */
@Slf4j
@Component
public class ChatWebSocketServer extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new JacksonObjectMapper();

    /** userId → WebSocketSession（单机版） */
    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    // ==================== 连接管理 ====================

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            try {
                session.close();
            } catch (Exception ignored) {
            }
            return;
        }
        SESSIONS.put(userId, session);
        log.info("WebSocket 连接建立：userId={}, 当前在线={}", userId, SESSIONS.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSIONS.remove(userId);
            log.info("WebSocket 连接关闭：userId={}, 当前在线={}", userId, SESSIONS.size());
        }
    }

    // ==================== 消息处理 ====================

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 只处理心跳，业务消息走 HTTP
        try {
            String payload = message.getPayload();
            Map<String, Object> map = objectMapper.readValue(payload, Map.class);
            String type = (String) map.get("type");

            if (ChatConstant.WS_TYPE_PING.equals(type)) {
                session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
            }
        } catch (Exception e) {
            log.error("WebSocket 处理消息失败", e);
        }
    }

    // ==================== 推送 ====================

    /**
     * 推送消息给指定用户
     */
    public void pushToUser(Long userId, Object payload) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("WebSocket 推送失败：userId={}", userId, e);
        }
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        return SESSIONS.size();
    }
}