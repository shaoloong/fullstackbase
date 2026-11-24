package com.example.base.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果的统一形状，前后端约定为：{ items, total, page, size }
 *
 * 为什么单独包一层而不是直接把 MyBatis-Plus 的 Page 对象返回前端：
 *   ① Page 里带一堆框架内部字段（orders、optimizeCountSql 等），前端不需要
 *   ② 换持久层（JPA / 手写 SQL）时前端不用跟着改
 *   ③ 字段命名能按前端习惯来，而不是被框架牵着走
 */
@Data
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    /** 当前页的数据列表 */
    private List<T> items;

    /** 符合条件的总条数（用于算总页数） */
    private long total;

    /** 当前页码，从 1 开始 */
    private long page;

    /** 每页条数 */
    private long size;
}
