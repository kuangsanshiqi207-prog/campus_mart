-- ========================================
-- 商品分类表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT       NOT NULL COMMENT '主键',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类表';