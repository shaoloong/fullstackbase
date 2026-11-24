package com.example.base.modules.__domain__.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * __title__实体：一张表对应一个类，字段与列一一对应。
 *
 * ⚠️ 生成后必改：把 name / status 换成真实业务字段，并同步修改
 *    - db/migration 里新建的建表脚本
 *    - __Domain__VO（对外字段）、__Domain__Query（查询条件）
 *    - __Domain__ServiceImpl 的 toVO 与条件构造器
 *
 * 实体不要直接返回前端：含 deleted 等内部字段，且 Long id 会丢精度。
 */
@Data
@TableName("__table__")
public class __Domain__ {

    /** 主键；演示用数据库自增，生产分布式系统建议 IdType.ASSIGN_ID（雪花） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 名称：几乎所有业务表都有，作为关键词搜索的默认列 */
    private String name;

    /** 状态：1 启用 / 0 停用；按业务改成自己的枚举值 */
    private Integer status;

    /** 逻辑删除：查询自动追加 deleted=0，删除变成 UPDATE，不物理删数据 */
    @TableLogic
    private Integer deleted;

    /** 用 LocalDateTime，别再用老项目的 java.util.Date */
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
