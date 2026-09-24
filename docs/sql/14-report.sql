-- ========================================
-- 举报表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `report` (
    `id`            BIGINT       NOT NULL COMMENT '主键',
    `reporter_id`   BIGINT       NOT NULL COMMENT '举报人ID',
    `target_type`   VARCHAR(20)  NOT NULL COMMENT '举报对象类型 product/user/order',
    `target_id`     BIGINT       NOT NULL COMMENT '举报对象ID',
    `reason`        VARCHAR(100) NOT NULL COMMENT '举报原因（短）',
    `description`   VARCHAR(500) DEFAULT NULL COMMENT '详细描述',
    `images`        VARCHAR(2000) DEFAULT NULL COMMENT '证据图片URL列表，逗号分隔',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '状态 pending/valid/invalid',
    `action`        VARCHAR(20)  DEFAULT NULL COMMENT '处理动作 none/offline/ban/warn',
    `handle_reason` VARCHAR(255) DEFAULT NULL COMMENT '处理说明',
    `handler_id`    BIGINT       DEFAULT NULL COMMENT '处理管理员ID',
    `handle_time`   DATETIME     DEFAULT NULL COMMENT '处理时间',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '举报表';

-- ========================================
-- 申诉表
-- ========================================

CREATE TABLE IF NOT EXISTS `appeal` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `report_id`   BIGINT       NOT NULL COMMENT '举报ID',
    `user_id`     BIGINT       NOT NULL COMMENT '申诉人ID（被举报人）',
    `reason`      VARCHAR(500) NOT NULL COMMENT '申诉理由',
    `images`      VARCHAR(2000) DEFAULT NULL COMMENT '证据图片URL列表',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
    `handle_reason` VARCHAR(255) DEFAULT NULL COMMENT '处理说明',
    `handler_id`  BIGINT       DEFAULT NULL COMMENT '处理管理员ID',
    `handle_time` DATETIME     DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_report` (`report_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '申诉表';