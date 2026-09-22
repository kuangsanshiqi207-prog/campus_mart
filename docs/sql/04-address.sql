-- ========================================
-- 收货地址表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `address` (
    `id`           BIGINT       NOT NULL COMMENT '主键',
    `user_id`      BIGINT       NOT NULL COMMENT '用户ID',
    `receiver`     VARCHAR(50)  NOT NULL COMMENT '收货人',
    `phone`        VARCHAR(20)  NOT NULL COMMENT '手机号',
    `region`       VARCHAR(100) DEFAULT NULL COMMENT '地区',
    `detail`       VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认 0否 1是',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收货地址表';