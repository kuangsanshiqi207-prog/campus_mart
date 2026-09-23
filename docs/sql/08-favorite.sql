-- ========================================
-- 收藏表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `favorite` (
    `id`          BIGINT   NOT NULL COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `product_id`  BIGINT   NOT NULL COMMENT '商品ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收藏表';