package com.example.base.modules.user.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 对外返回的用户视图对象（View Object）。
 *
 * 实体 → VO 这一步不能省，它承担三件事：
 *   ① 裁剪字段：不把 deleted、updatedAt 这些内部字段暴露出去
 *   ② 转换类型：Long id 转 String，避免 JS 超过 2^53 后精度丢失
 *   ③ 补充展示字段：statusLabel、格式化好的 createdAt，前端不用再算一遍
 */
@Data
public class UserVO implements Serializable {

    /** ⚠️ 刻意用 String：后端 Long 雪花 ID 直接给前端会变成 ...000 */
    private String id;

    private String name;

    private String phone;

    private String email;

    /** 原始状态值，供前端做逻辑判断 */
    private Integer status;

    /** 直接可展示的中文标签，前端不用维护一份状态字典 */
    private String statusLabel;

    /** 已格式化成 yyyy-MM-dd HH:mm:ss 的字符串，绕开前后端时区差异 */
    private String createdAt;
}
