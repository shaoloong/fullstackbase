package com.example.base.modules.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 产品列表查询入参。
 *
 * ⚠️ Boot 3 起校验注解包名是 jakarta.validation（不是 javax），写错直接编译失败。
 * Spring MVC 会把 ?page=1&size=10&keyword=xx&status=1 按字段名自动绑定进来。
 */
@Data
public class ProductQuery {

    /** 页码，从 1 开始；给默认值是为了前端不传也能工作 */
    @Min(value = 1, message = "页码最小为 1")
    private Integer page = 1;

    /** 每页条数；@Max 防止有人传 size=100000 把库拖垮 */
    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 100, message = "每页条数最多 100")
    private Integer size = 10;

    /** 关键词：同时匹配 name 与 code，为空表示不筛选 */
    private String keyword;

    /** 状态筛选：null 表示全部，1 启用，0 停用 */
    private Integer status;
}
