package com.example.base.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库实体：一张表对应一个类，字段一一对应（前端可理解为 ORM 版的 interface）。
 *
 * ⚠️ 实体不要直接返回给前端：这里含 deleted、updatedAt 等内部字段，
 *    而且 Long 类型的 id 直接序列化会丢精度；对外一律用 UserVO。
 */
@Data
@TableName("t_user")
public class User {

    /** 主键：演示用数据库自增；生产分布式系统多用雪花 ID（IdType.ASSIGN_ID） */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String phone;

    private String email;

    /** 状态：1 启用 / 0 停用 */
    private Integer status;

    /**
     * 逻辑删除标记。
     * 标注后 MP 会自动：查询追加 WHERE deleted = 0、删除变成 UPDATE deleted = 1，
     * 数据不会真的从库里消失，便于恢复和审计。
     */
    @TableLogic
    private Integer deleted;

    /** 用 LocalDateTime（Java 8+），别再用老项目里的 java.util.Date */
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
