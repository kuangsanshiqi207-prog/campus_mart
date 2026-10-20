-- ========================================
-- AI 审核日志表
-- 执行：mysql -u root -p < docs/sql/18-ai-audit-log.sql
-- ========================================

USE campus_mart;

CREATE TABLE IF NOT EXISTS `ai_audit_log` (
    `id`               BIGINT       NOT NULL COMMENT '主键（雪花ID）',
    `audit_type`       VARCHAR(20)  NOT NULL COMMENT '审核类型 product/review/report/certification',
    `biz_id`           BIGINT       NOT NULL COMMENT '被审核对象ID',
    `passed`           TINYINT      DEFAULT NULL COMMENT '是否通过 1通过/0驳回/NULL无结果',
    `reason`           VARCHAR(500) DEFAULT NULL COMMENT '判定理由',
    `risk_category`    VARCHAR(50)  DEFAULT NULL COMMENT '风险类别',
    `risk_level`       VARCHAR(20)  DEFAULT NULL COMMENT '风险等级 low/medium/high',
    `suggested_action` VARCHAR(50)  DEFAULT NULL COMMENT '建议处置',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_audit_type_biz` (`audit_type`, `biz_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 审核日志';
