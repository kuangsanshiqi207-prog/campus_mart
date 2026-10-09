package com.example.controller;

import com.example.api.message.MessageFeignClient;
import com.example.entity.Message;
import com.example.mapper.message.MessageMapper;
import com.example.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import cn.hutool.core.util.IdUtil;


@RestController
@RequestMapping("/internal/message")
@RequiredArgsConstructor
public class MessageInternalController implements MessageFeignClient {

    private final MessageMapper messageMapper;

    @Override
    public Result<Void> send(Long toUserId, String type, String title, String content, Long bizId) {
        Message msg = Message.builder()
                .id(IdUtil.getSnowflakeNextId())
                .userId(toUserId)
                .type(type)
                .title(title)
                .content(content)
                .bizId(bizId)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        messageMapper.insert(msg);
        return Result.success();
    }

    @Override
    public Result<Long> countUnread(Long userId) {
        Long count = messageMapper.countUnread(userId);
        return Result.success(count);
    }
}