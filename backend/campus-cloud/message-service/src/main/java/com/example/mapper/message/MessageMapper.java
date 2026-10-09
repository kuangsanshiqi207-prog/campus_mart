package com.example.mapper.message;

import com.example.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    Message getById(Long id);

    void insert(Message message);

    List<Message> listByUserId(@Param("userId") Long userId,
                               @Param("type") String type,
                               @Param("isRead") Integer isRead,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);

    Long countByUserId(@Param("userId") Long userId,
                       @Param("type") String type,
                       @Param("isRead") Integer isRead);

    Long countUnread(Long userId);

    void markRead(@Param("id") Long id);

    void markAllRead(Long userId);

    void deleteById(Long id);
}