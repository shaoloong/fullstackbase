package com.example.base.modules.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 产品对外视图对象。
 *
 * 实体 → VO 承担三件事：
 *   ① 裁剪字段：不暴露 deleted / updatedAt
 *   ② 转换类型：Long id → String，避免 JS 超过 2^53 丢精度
 *   ③ 补充展示字段：statusLabel、格式化好的 createdAt
 */
@Data
public class ProductVO implements Serializable {

    /** 刻意用 String：Long id 直接给前端会丢精度 */
    private String id;

    private String name;

    private String code;

    private String category;

    private BigDecimal price;

    private Integer stock;

    private String description;

    /** 原始状态值，供前端做逻辑判断 */
    private Integer status;

    /** 直接可展示的中文标签 */
    private String statusLabel;

    /** 已格式化成 yyyy-MM-dd HH:mm:ss，绕开前后端时区差异 */
    private String createdAt;
}
