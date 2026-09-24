package com.example.service.message.impl;

import cn.hutool.core.util.IdUtil;
import com.example.constant.MessageConstant;
import com.example.context.BaseContext;
import com.example.entity.Announcement;
import com.example.entity.Message;
import com.example.exception.BaseException;
import com.example.mapper.message.AnnouncementMapper;
import com.example.mapper.message.MessageMapper;
import com.example.result.PageResult;
import com.example.service.message.MessageSender;
import com.example.service.message.MessageService;
import com.example.vo.message.AnnouncementVO;
import com.example.vo.message.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService, MessageSender {

    private final MessageMapper messageMapper;
    private final AnnouncementMapper announcementMapper;

    // ==================== 发送消息（内部调用） ====================

    @Override
    public void send(Long toUserId, String type, String title, String content, Long bizId) {
        if (toUserId == null) return;

        Message message = Message.builder()
                .id(IdUtil.getSnowflakeNextId())
                .userId(toUserId)
                .type(type)
                .title(title)
                .content(content)
                .bizId(bizId)
                .createTime(LocalDateTime.now())
                .build();

        messageMapper.insert(message);
        log.info("消息已发送：to={}, type={}, title={}", toUserId, type, title);
    }

    // ==================== 消息查询 ====================

    @Override
    public PageResult listMessages(String type, Integer isRead, Integer pageNum, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        pageNum = normalizePageNum(pageNum);
        pageSize = normalizePageSize(pageSize);

        int offset = (pageNum - 1) * pageSize;
        Long total = messageMapper.countByUserId(userId, type, isRead);
        List<Message> list = messageMapper.listByUserId(userId, type, isRead, offset, pageSize);

        List<MessageVO> voList = list.stream()
                .map(this::toMessageVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        return messageMapper.countUnread(userId);
    }

    // ==================== 消息操作 ====================

    @Override
    public void markRead(Long id) {
        Long userId = BaseContext.getCurrentId();
        Message message = messageMapper.getById(id);
        if (message == null) {
            throw new BaseException(MessageConstant.MESSAGE_NOT_FOUND);
        }
        if (!message.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.MESSAGE_NO_PERMISSION);
        }
        messageMapper.markRead(id);
    }

    @Override
    public void markAllRead() {
        Long userId = BaseContext.getCurrentId();
        messageMapper.markAllRead(userId);
    }

    @Override
    public void deleteMessage(Long id) {
        Long userId = BaseContext.getCurrentId();
        Message message = messageMapper.getById(id);
        if (message == null) {
            throw new BaseException(MessageConstant.MESSAGE_NOT_FOUND);
        }
        if (!message.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.MESSAGE_NO_PERMISSION);
        }
        messageMapper.deleteById(id);
    }

    // ==================== 公告 ====================

    @Override
    public PageResult listAnnouncements(Integer pageNum, Integer pageSize) {
        pageNum = normalizePageNum(pageNum);
        pageSize = normalizePageSize(pageSize);

        int offset = (pageNum - 1) * pageSize;
        Long total = announcementMapper.countPublished();
        List<Announcement> list = announcementMapper.listPublished(offset, pageSize);

        List<AnnouncementVO> voList = list.stream()
                .map(this::toAnnouncementVO)
                .collect(Collectors.toList());

        return new PageResult(total, voList);
    }

    @Override
    public AnnouncementVO getAnnouncement(Long id) {
        Announcement announcement = announcementMapper.getById(id);
        if (announcement == null) {
            throw new BaseException("公告不存在");
        }
        return toAnnouncementVO(announcement);
    }

    // ==================== 私有方法 ====================

    private Integer normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) return 1;
        return pageNum;
    }

    private Integer normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1 || pageSize > 100) return 10;
        return pageSize;
    }

    private MessageVO toMessageVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }

    private AnnouncementVO toAnnouncementVO(Announcement announcement) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(announcement, vo);
        return vo;
    }
}