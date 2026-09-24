package com.example.service.chat.impl;

import cn.hutool.core.util.IdUtil;
import com.example.constant.ChatConstant;
import com.example.constant.MessageConstant;
import com.example.context.BaseContext;
import com.example.dto.chat.ChatSendDTO;
import com.example.dto.chat.ConversationCreateDTO;
import com.example.entity.ChatMessage;
import com.example.entity.Conversation;
import com.example.entity.User;
import com.example.exception.BaseException;
import com.example.mapper.chat.ChatMessageMapper;
import com.example.mapper.chat.ConversationMapper;
import com.example.mapper.user.UserMapper;
import com.example.service.chat.ChatPushService;
import com.example.service.chat.ChatService;
import com.example.vo.chat.ChatMessageVO;
import com.example.vo.chat.ConversationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final ChatPushService chatPushService;

    // ==================== 创建/获取会话 ====================

    @Override
    @Transactional
    public ConversationVO createOrGetConversation(ConversationCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();
        Long targetUserId = dto.getTargetUserId();

        if (userId.equals(targetUserId)) {
            throw new BaseException(MessageConstant.CHAT_CANNOT_SEND_TO_SELF);
        }
        User targetUser = userMapper.getById(targetUserId);
        if (targetUser == null) {
            throw new BaseException(MessageConstant.CHAT_TARGET_USER_NOT_FOUND);
        }

        // 保证小 ID 在前
        Long userAId = Math.min(userId, targetUserId);
        Long userBId = Math.max(userId, targetUserId);

        Conversation conversation = conversationMapper.getByUserPair(userAId, userBId);
        if (conversation == null) {
            conversation = Conversation.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .userAId(userAId)
                    .userBId(userBId)
                    .lastMessage(null)
                    .lastMessageAt(LocalDateTime.now())
                    .aUnread(0)
                    .bUnread(0)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            conversationMapper.insert(conversation);
        }

        return toConversationVO(conversation, userId);
    }

    // ==================== 会话列表 ====================

    @Override
    public List<ConversationVO> listConversations() {
        Long userId = BaseContext.getCurrentId();
        List<Conversation> list = conversationMapper.listByUserId(userId);
        return list.stream()
                .map(c -> toConversationVO(c, userId))
                .collect(Collectors.toList());
    }

    // ==================== 历史消息 ====================

    @Override
    public List<ChatMessageVO> listMessages(Long conversationId, Long lastId, Integer limit) {
        Long userId = BaseContext.getCurrentId();
        Conversation conversation = getConversationOrThrow(conversationId);
        if (!isParticipant(conversation, userId)) {
            throw new BaseException(MessageConstant.CONVERSATION_NO_PERMISSION);
        }
        if (limit == null || limit < 1 || limit > 100) limit = 20;

        List<ChatMessage> list = chatMessageMapper.listByConversation(conversationId, lastId, limit);
        // 返回时倒序转正序，前端渲染方便
        List<ChatMessageVO> result = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            result.add(toChatMessageVO(list.get(i)));
        }
        return result;
    }

    // ==================== 发送消息 ====================

    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long conversationId, Long fromUserId, ChatSendDTO dto) {
        Conversation conversation = getConversationOrThrow(conversationId);
        if (!isParticipant(conversation, fromUserId)) {
            throw new BaseException(MessageConstant.CONVERSATION_NO_PERMISSION);
        }

        // 确定接收者
        Long toUserId = conversation.getUserAId().equals(fromUserId)
                ? conversation.getUserBId()
                : conversation.getUserAId();

        // 构造消息
        ChatMessage message = ChatMessage.builder()
                .id(IdUtil.getSnowflakeNextId())
                .conversationId(conversationId)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .type(dto.getType() != null ? dto.getType() : ChatConstant.MSG_TYPE_TEXT)
                .content(dto.getContent())
                .status(ChatConstant.MSG_STATUS_SENT)
                .createTime(LocalDateTime.now())
                .build();
        chatMessageMapper.insert(message);

        // 更新会话最后消息 + 未读数
        String preview = dto.getContent();
        if (preview.length() > 100) preview = preview.substring(0, 100) + "...";
        conversationMapper.updateLastMessage(conversationId, preview);

        boolean targetIsA = conversation.getUserAId().equals(toUserId);
        conversationMapper.incrementUnread(conversationId, targetIsA);

        ChatMessageVO vo = toChatMessageVO(message);

        // WebSocket 推送
        chatPushService.pushNewMessage(toUserId, vo);

        return vo;
    }

    // ==================== 标记已读 ====================

    @Override
    @Transactional
    public void markRead(Long conversationId) {
        Long userId = BaseContext.getCurrentId();
        Conversation conversation = getConversationOrThrow(conversationId);
        if (!isParticipant(conversation, userId)) {
            throw new BaseException(MessageConstant.CONVERSATION_NO_PERMISSION);
        }

        chatMessageMapper.markRead(conversationId, userId);

        boolean targetIsA = conversation.getUserAId().equals(userId);
        conversationMapper.clearUnread(conversationId, targetIsA);

        // 通知对方：我读了
        Long otherUserId = conversation.getUserAId().equals(userId)
                ? conversation.getUserBId()
                : conversation.getUserAId();
        chatPushService.pushReadReceipt(otherUserId, conversationId);
    }

    // ==================== 删除会话 ====================

    @Override
    public void deleteConversation(Long conversationId) {
        Long userId = BaseContext.getCurrentId();
        Conversation conversation = getConversationOrThrow(conversationId);
        if (!isParticipant(conversation, userId)) {
            throw new BaseException(MessageConstant.CONVERSATION_NO_PERMISSION);
        }
        // 逻辑删除：需要 Mapper 提供 deleteById 方法
        conversationMapper.deleteById(conversationId);
    }

    // ==================== 私有方法 ====================

    private Conversation getConversationOrThrow(Long id) {
        Conversation conversation = conversationMapper.getById(id);
        if (conversation == null) {
            throw new BaseException(MessageConstant.CONVERSATION_NOT_FOUND);
        }
        return conversation;
    }

    private boolean isParticipant(Conversation conversation, Long userId) {
        return conversation.getUserAId().equals(userId)
                || conversation.getUserBId().equals(userId);
    }

    private ConversationVO toConversationVO(Conversation conversation, Long currentUserId) {
        Long targetUserId = conversation.getUserAId().equals(currentUserId)
                ? conversation.getUserBId()
                : conversation.getUserAId();

        User targetUser = userMapper.getById(targetUserId);

        Integer unread = conversation.getUserAId().equals(currentUserId)
                ? conversation.getAUnread()
                : conversation.getBUnread();

        return ConversationVO.builder()
                .id(conversation.getId())
                .targetUserId(targetUserId)
                .targetNickname(targetUser != null ? targetUser.getNickname() : "已注销")
                .targetAvatar(targetUser != null ? targetUser.getAvatar() : null)
                .lastMessage(conversation.getLastMessage())
                .lastMessageAt(conversation.getLastMessageAt())
                .unread(unread)
                .build();
    }

    private ChatMessageVO toChatMessageVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }
}