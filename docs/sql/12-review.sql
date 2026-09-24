-- ========================================
-- 评价表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `review` (
    `id`             BIGINT       NOT NULL COMMENT '主键',
    `order_id`       BIGINT       NOT NULL COMMENT '订单ID',
    `product_id`     BIGINT       NOT NULL COMMENT '商品ID',
    `from_user_id`   BIGINT       NOT NULL COMMENT '评价人ID',
    `to_user_id`     BIGINT       NOT NULL COMMENT '被评价人ID',
    `score`          TINYINT      NOT NULL COMMENT '评分 1-5',
    `content`        VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `images`         VARCHAR(2000) DEFAULT NULL COMMENT '图片URL列表，逗号分隔',
    `reply`          VARCHAR(500) DEFAULT NULL COMMENT '被评价人的回复',
    `reply_time`     DATETIME     DEFAULT NULL COMMENT '回复时间',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_from` (`order_id`, `from_user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_to_user_id` (`to_user_id`),
    KEY `idx_from_user_id` (`from_user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '评价表';