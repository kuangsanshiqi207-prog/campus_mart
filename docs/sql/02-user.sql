-- ========================================
-- 用户表
-- 用途：认证模块（注册、登录、登出、重置密码）
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `user` (
    `id`           BIGINT       NOT NULL COMMENT '用户ID',
    `username`     VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`     VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    `nickname`     VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`       VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `phone`        VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`        VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role`         VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色 USER/ADMIN',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'normal' COMMENT '状态 normal/banned',
    `certified`    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否校园认证 0否 1是',
    `credit_score` INT          NOT NULL DEFAULT 100 COMMENT '信用分',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';