-- ========================================
-- 浏览历史表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `browse_history` (
    `id`          BIGINT   NOT NULL COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `product_id`  BIGINT   NOT NULL COMMENT '商品ID',
    `browse_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '浏览时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_time` (`user_id`, `browse_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '浏览历史表';