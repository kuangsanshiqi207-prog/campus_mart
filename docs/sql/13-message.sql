-- ========================================
-- 消息表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `message` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '接收者ID',
    `type`        VARCHAR(20)  NOT NULL COMMENT '消息类型 order/audit/report/system',
    `title`       VARCHAR(100) NOT NULL COMMENT '标题',
    `content`     VARCHAR(500) NOT NULL COMMENT '内容',
    `biz_id`      BIGINT       DEFAULT NULL COMMENT '关联业务ID（订单ID/商品ID等）',
    `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '消息表';

-- ========================================
-- 公告表
-- ========================================

CREATE TABLE IF NOT EXISTS `announcement` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `title`       VARCHAR(100) NOT NULL COMMENT '标题',
    `content`     TEXT         NOT NULL COMMENT '内容',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'published' COMMENT 'draft/published',
    `admin_id`    BIGINT       DEFAULT NULL COMMENT '发布管理员ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告表';