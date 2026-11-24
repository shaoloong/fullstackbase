-- ============================================================
-- V1__init.sql 建表脚本（Flyway 按文件名版本号执行，只跑一次）
-- 兼容策略：不用 MySQL 专有语法（如列内联 COMMENT、ENGINE=InnoDB），
--          因此同一份脚本既能跑在 H2（MODE=MySQL）也能跑在真实 MySQL 8
-- ============================================================

CREATE TABLE t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,   -- 主键；演示用自增，生产多用雪花 ID
    name        VARCHAR(64)  NOT NULL,                 -- 姓名
    phone       VARCHAR(20),                          -- 手机号
    email       VARCHAR(128),                         -- 邮箱
    status      TINYINT      NOT NULL DEFAULT 1,      -- 状态：1 启用 / 0 停用
    deleted     TINYINT      NOT NULL DEFAULT 0,      -- 逻辑删除：0 正常 / 1 已删除
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 更新时间
    PRIMARY KEY (id)
);

-- 姓名加索引：列表按姓名模糊查询时用得上
-- 注意：LIKE '%xx%' 前缀带 % 用不上索引，本工程前端搜索同样受此限制
CREATE INDEX idx_user_name ON t_user (name);

-- 状态 + 创建时间联合索引：带状态筛选 + 按时间排序的组合查询更常见
CREATE INDEX idx_user_status_created ON t_user (status, created_at);
