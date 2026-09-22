-- ========================================
-- 校园认证表
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `user_certification` (
    `id`           BIGINT       NOT NULL COMMENT '主键',
    `user_id`      BIGINT       NOT NULL COMMENT '用户ID',
    `school`       VARCHAR(100) NOT NULL COMMENT '学校',
    `student_no`   VARCHAR(50)  NOT NULL COMMENT '学号',
    `real_name`    VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    `card_image`   VARCHAR(500) DEFAULT NULL COMMENT '学生证图片URL',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/rejected',
    `reason`       VARCHAR(255) DEFAULT NULL COMMENT '审核拒绝原因',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '校园认证表';