-- ========================================
-- 会话表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `conversation` (
    `id`              BIGINT   NOT NULL COMMENT '主键',
    `user_a_id`       BIGINT   NOT NULL COMMENT '用户A（小ID）',
    `user_b_id`       BIGINT   NOT NULL COMMENT '用户B（大ID）',
    `last_message`    VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息预览',
    `last_message_at` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `a_unread`        INT      NOT NULL DEFAULT 0 COMMENT 'A的未读数',
    `b_unread`        INT      NOT NULL DEFAULT 0 COMMENT 'B的未读数',
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_pair` (`user_a_id`, `user_b_id`),
    KEY `idx_user_a` (`user_a_id`),
    KEY `idx_user_b` (`user_b_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会话表';

-- ========================================
-- 消息表
-- ========================================

CREATE TABLE IF NOT EXISTS `chat_message` (
    `id`              BIGINT       NOT NULL COMMENT '主键',
    `conversation_id` BIGINT       NOT NULL COMMENT '会话ID',
    `from_user_id`    BIGINT       NOT NULL COMMENT '发送者',
    `to_user_id`      BIGINT       NOT NULL COMMENT '接收者',
    `type`            VARCHAR(20)  NOT NULL DEFAULT 'text' COMMENT 'text/image/product/order',
    `content`         VARCHAR(2000) NOT NULL COMMENT '消息内容',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'sent' COMMENT 'sent/read',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`, `id`),
    KEY `idx_to_user_read` (`to_user_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '聊天消息表';