package com.example.service.message;

import com.example.result.PageResult;
import com.example.vo.message.AnnouncementVO;
import com.example.vo.message.MessageVO;

public interface MessageService {

    PageResult listMessages(String type, Integer isRead, Integer pageNum, Integer pageSize);

    Long getUnreadCount();

    void markRead(Long id);

    void markAllRead();

    void deleteMessage(Long id);

    PageResult listAnnouncements(Integer pageNum, Integer pageSize);

    AnnouncementVO getAnnouncement(Long id);
}