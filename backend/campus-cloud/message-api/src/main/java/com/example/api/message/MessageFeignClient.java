package com.example.api.message;

import com.example.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "message-service", path = "/internal/message")
public interface MessageFeignClient {

    /**
     * 发送站内信
     *
     * @param toUserId  接收者
     * @param type      消息类型（order/audit/report/system）
     * @param title     标题
     * @param content   内容
     * @param bizId     关联业务 ID（订单 ID / 商品 ID 等）
     */
    @PostMapping("/send")
    Result<Void> send(@RequestParam("toUserId") Long toUserId,
                      @RequestParam("type") String type,
                      @RequestParam("title") String title,
                      @RequestParam("content") String content,
                      @RequestParam(value = "bizId", required = false) Long bizId);

    /** 查询用户未读消息数 */
    @GetMapping("/countUnread")
    Result<Long> countUnread(@RequestParam("userId") Long userId);
}