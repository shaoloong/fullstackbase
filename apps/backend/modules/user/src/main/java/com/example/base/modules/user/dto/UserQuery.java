package com.example.base.modules.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 列表查询入参 DTO（Data Transfer Object）。
 *
 * ⚠️ 包名必须是 jakarta.validation（Boot 3 起已从 javax 迁移）；
 *    照抄老教程写 javax.validation 在本工程里会直接编译失败。
 *
 * Spring MVC 会把 ?page=1&size=10&keyword=xx 自动按字段名绑定到这个对象。
 */
@Data
public class UserQuery {

    /** 页码，从 1 开始；给默认值是为了前端不传也能正常工作 */
    @Min(value = 1, message = "页码最小为 1")
    private Integer page = 1;

    /** 每页条数；加 @Max 防止有人传 size=100000 把库拖垮 */
    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 100, message = "每页条数最多 100")
    private Integer size = 10;

    /** 关键词：同时匹配姓名 / 手机号 / 邮箱，为空表示不筛选 */
    private String keyword;

    /** 状态筛选：null 表示全部，1 启用，0 停用 */
    private Integer status;
}
