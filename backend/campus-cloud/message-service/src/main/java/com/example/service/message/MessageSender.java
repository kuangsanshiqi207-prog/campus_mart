package com.example.service.message;

/**
 * 消息发送器，供其他模块（订单、审核等）调用
 */
public interface MessageSender {

    /**
     * 发送交易相关消息
     *
     * @param toUserId  接收者
     * @param type      消息类型
     * @param title     标题
     * @param content   内容
     * @param bizId     关联业务ID
     */
    void send(Long toUserId, String type, String title, String content, Long bizId);
}