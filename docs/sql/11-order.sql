-- ========================================
-- 订单表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `order` (
    `id`               BIGINT        NOT NULL COMMENT '主键',
    `order_no`         VARCHAR(32)   NOT NULL COMMENT '订单号',
    `product_id`       BIGINT        NOT NULL COMMENT '商品ID',
    `buyer_id`         BIGINT        NOT NULL COMMENT '买家ID',
    `seller_id`        BIGINT        NOT NULL COMMENT '卖家ID',
    `amount`           DECIMAL(10,2) NOT NULL COMMENT '成交金额',
    `status`           VARCHAR(20)   NOT NULL DEFAULT 'pending' COMMENT '订单状态',
    `trade_type`       VARCHAR(20)   DEFAULT NULL COMMENT '交易方式',
    `trade_place`      VARCHAR(100)  DEFAULT NULL COMMENT '交易地点',
    `appointment_time` DATETIME      DEFAULT NULL COMMENT '约定时间',
    `remark`           VARCHAR(500)  DEFAULT NULL COMMENT '买家留言',
    `reject_reason`    VARCHAR(255)  DEFAULT NULL COMMENT '卖家拒绝原因',
    `cancel_reason`    VARCHAR(255)  DEFAULT NULL COMMENT '取消原因',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `completed_time`   DATETIME      DEFAULT NULL COMMENT '完成时间',
    `deleted`          TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';