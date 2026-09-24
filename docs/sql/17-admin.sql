-- ========================================
-- 管理员表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `admin` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' COMMENT '角色 SUPER_ADMIN/ADMIN',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'normal' COMMENT '状态 normal/banned',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '管理员表';

-- 初始化一个超级管理员：admin / 123456
INSERT INTO `admin` (id, username, password, nickname, role, status)
VALUES (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '超级管理员', 'SUPER_ADMIN', 'normal')
ON DUPLICATE KEY UPDATE `username` = `username`;