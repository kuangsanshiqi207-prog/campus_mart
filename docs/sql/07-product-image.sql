-- ========================================
-- 商品图片表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `product_image` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `product_id`  BIGINT       NOT NULL COMMENT '商品ID',
    `url`         VARCHAR(500) NOT NULL COMMENT '图片URL',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序，0为封面',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品图片表';