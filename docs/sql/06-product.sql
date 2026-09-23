-- ========================================
-- 商品表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `product` (
    `id`             BIGINT        NOT NULL COMMENT '主键',
    `seller_id`      BIGINT        NOT NULL COMMENT '卖家ID',
    `category_id`    BIGINT        NOT NULL COMMENT '分类ID',
    `title`          VARCHAR(100)  NOT NULL COMMENT '标题',
    `description`    TEXT          COMMENT '描述',
    `price`          DECIMAL(10,2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `quality`      VARCHAR(20)   NOT NULL DEFAULT 'good' COMMENT '成色 new/almost_new/good/normal',
    `trade_type`     VARCHAR(20)   NOT NULL DEFAULT 'face' COMMENT '交易方式 face/express/self_pickup',
    `trade_place`    VARCHAR(100)  DEFAULT NULL COMMENT '交易地点',
    `status`         VARCHAR(20)   NOT NULL DEFAULT 'on_sale' COMMENT '上架状态 on_sale/reserved/sold/offline',
    `audit_status`   VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT '审核状态 pending/approved/rejected',
    `audit_reason`   VARCHAR(255)  DEFAULT NULL COMMENT '审核拒绝原因',
    `view_count`     INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
    `favorite_count` INT           NOT NULL DEFAULT 0 COMMENT '收藏数',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';