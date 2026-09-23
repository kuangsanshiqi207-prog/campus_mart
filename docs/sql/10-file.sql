-- ========================================
-- 文件记录表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `file` (
    `id`            BIGINT       NOT NULL COMMENT '主键',
    `user_id`       BIGINT       NOT NULL COMMENT '上传者ID',
    `url`           VARCHAR(500) NOT NULL COMMENT '访问URL',
    `original_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `file_size`     BIGINT       DEFAULT NULL COMMENT '文件大小(字节)',
    `content_type`  VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'unused' COMMENT '状态 unused/used',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文件记录表';