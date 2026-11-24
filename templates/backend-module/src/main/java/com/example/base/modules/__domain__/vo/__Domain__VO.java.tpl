package com.example.base.modules.__domain__.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * __title__对外视图对象。
 *
 * 实体 → VO 这一步不能省，承担三件事：
 *   ① 裁剪字段：不暴露 deleted / updatedAt
 *   ② 转换类型：Long id → String，避免 JS 超过 2^53 丢精度
 *   ③ 补充展示字段：statusLabel、格式化好的 createdAt，前端不用再算
 */
@Data
public class __Domain__VO implements Serializable {

    /** 刻意用 String：雪花 ID 直接给前端会变成 ...000 */
    private String id;

    private String name;

    /** 原始状态值，供前端做逻辑判断 */
    private Integer status;

    /** 直接可展示的中文标签 */
    private String statusLabel;

    /** 已格式化成 yyyy-MM-dd HH:mm:ss，绕开前后端时区差异 */
    private String createdAt;
}
