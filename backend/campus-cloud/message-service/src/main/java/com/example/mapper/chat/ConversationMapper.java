package com.example.mapper.chat;

import com.example.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationMapper {

    Conversation getById(Long id);

    Conversation getByUserPair(@Param("userAId") Long userAId,
                               @Param("userBId") Long userBId);

    void insert(Conversation conversation);

    void updateLastMessage(@Param("id") Long id,
                           @Param("lastMessage") String lastMessage);

    void incrementUnread(@Param("id") Long id,
                         @Param("targetIsA") boolean targetIsA);

    void clearUnread(@Param("id") Long id,
                     @Param("targetIsA") boolean targetIsA);

    List<Conversation> listByUserId(Long userId);

    void deleteById(Long conversationId);
}