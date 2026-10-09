package com.example.mapper.chat;

import com.example.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    void insert(ChatMessage message);

    List<ChatMessage> listByConversation(@Param("conversationId") Long conversationId,
                                         @Param("lastId") Long lastId,
                                         @Param("limit") Integer limit);

    void markRead(@Param("conversationId") Long conversationId,
                  @Param("toUserId") Long toUserId);
}