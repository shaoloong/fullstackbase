-- ============================================================
-- V3__product.sql —— 产品管理建表脚本（一次性写定，之后不得再改）
--
-- Flyway 铁律：
--   ① 版本号只增不改，已执行过的脚本**绝不能修改内容**（校验和会不匹配，启动即失败）
--   ② 需要改表就再新建一个 V<n+1>__xxx.sql，不要回头改老的
--   ③ 同一份脚本要能同时跑在 H2（MODE=MySQL）与真实 MySQL 8，
--      所以不用列内联 COMMENT、ENGINE=InnoDB 等 MySQL 专有写法
--   ④ 表名 t_ 前缀；金额一律 DECIMAL(p,s)，绝不用 FLOAT/DOUBLE
-- ============================================================

CREATE TABLE IF NOT EXISTS t_product (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(128)  NOT NULL,
    code        VARCHAR(64)   NOT NULL,
    category    VARCHAR(64),
    price       DECIMAL(10,2) NOT NULL,
    stock       INT           NOT NULL,
    description VARCHAR(500),
    status      TINYINT       NOT NULL DEFAULT 1,
    deleted     TINYINT       NOT NULL DEFAULT 0,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_code UNIQUE (code)
);

-- 状态筛选 + 创建时间排序的组合索引
-- 注意：MySQL 8.4 的 CREATE INDEX 不支持 IF NOT EXISTS，H2 虽接受但会掩盖方言不兼容；
-- 这里使用两库通用的标准语法（Flyway 保证本脚本只在版本升级时执行一次）。
CREATE INDEX idx_product_status_created ON t_product (status, created_at);

-- ⚠️ LIKE '%xx%' 前缀带 % 用不上普通索引；数据量大时改全文索引或搜索引擎
