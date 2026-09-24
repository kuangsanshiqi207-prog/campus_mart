-- ========================================
-- 信用分流水表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `credit_log` (
    `id`            BIGINT       NOT NULL COMMENT '主键',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `delta`         INT          NOT NULL COMMENT '变动值（正加负减）',
    `before_score`  INT          NOT NULL COMMENT '变动前信用分',
    `after_score`   INT          NOT NULL COMMENT '变动后信用分',
    `reason`        VARCHAR(255) NOT NULL COMMENT '变动原因',
    `biz_type`      VARCHAR(20)  DEFAULT NULL COMMENT '业务类型 order/report/review',
    `biz_id`        BIGINT       DEFAULT NULL COMMENT '业务ID',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '信用分流水表';