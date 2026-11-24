-- ============================================================
-- V<n>__<domain>.sql —— __title__模块建表脚本（由模板生成，文件名由生成器按版本号构造）
--
-- Flyway 铁律：
--   ① 版本号只增不改，已执行过的脚本**绝不能修改内容**（校验和会不匹配，启动即失败）
--   ② 需要改表就再新建一个 V<n+1>__xxx.sql，不要回头改老的
--   ③ 同一份脚本要能同时跑在 H2（MODE=MySQL）与真实 MySQL 8，
--      所以不用列内联 COMMENT、ENGINE=InnoDB 等 MySQL 专有写法
--   ④ 表名 t_ 前缀；列类型保守；金额一律 DECIMAL(p,s)，绝不用 FLOAT/DOUBLE
-- ============================================================

CREATE TABLE IF NOT EXISTS __table__ (
    id          BIGINT       NOT NULL AUTO_INCREMENT,   -- 主键
    name        VARCHAR(128) NOT NULL,                 -- 名称（生成后按业务替换字段）
    status      TINYINT      NOT NULL DEFAULT 1,       -- 状态：1 启用 / 0 停用
    deleted     TINYINT      NOT NULL DEFAULT 0,       -- 逻辑删除：0 正常 / 1 已删除
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 按实际查询条件补索引：这里是「状态筛选 + 按时间排序」的常见组合
CREATE INDEX IF NOT EXISTS idx___domain___status_created ON __table__ (status, created_at);

-- ⚠️ LIKE '%xx%' 前缀带 % 用不上普通索引；数据量大时改全文索引或搜索引擎
