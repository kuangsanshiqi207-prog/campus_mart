package com.example.service.chat;

import com.example.dto.chat.ChatSendDTO;
import com.example.dto.chat.ConversationCreateDTO;
import com.example.vo.chat.ChatMessageVO;
import com.example.vo.chat.ConversationVO;

import java.util.List;

public interface ChatService {

    ConversationVO createOrGetConversation(ConversationCreateDTO dto);

    List<ConversationVO> listConversations();

    List<ChatMessageVO> listMessages(Long conversationId, Long lastId, Integer limit);

    ChatMessageVO sendMessage(Long conversationId, Long fromUserId, ChatSendDTO dto);

    void markRead(Long conversationId);

    void deleteConversation(Long conversationId);
}
