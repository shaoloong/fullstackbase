package com.example.base.modules.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体：字段与 t_product 的列一一对应。
 *
 * 实体不要直接返回前端：含 deleted / updatedAt 等内部字段，
 * 且 Long id 直接序列化会丢精度；对外一律转 ProductVO。
 */
@Data
@TableName("t_product")
public class Product {

    /** 主键；数据库自增，对外输出时转成 String */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 产品名称 */
    private String name;

    /** 产品编码 / SKU，业务唯一键 */
    private String code;

    /** 产品分类 */
    private String category;

    /** 售价：DECIMAL(10,2)，禁止 FLOAT/DOUBLE */
    private BigDecimal price;

    /** 库存数量 */
    private Integer stock;

    /** 产品描述 */
    private String description;

    /** 状态：1 启用 / 0 停用 */
    private Integer status;

    /** 逻辑删除：查询自动追加 deleted=0，删除变成 UPDATE */
    @TableLogic
    private Integer deleted;

    /** 创建时间：由 DB 默认值填充 */
    private LocalDateTime createdAt;

    /** 更新时间：更新时由服务层显式赋值 */
    private LocalDateTime updatedAt;
}
