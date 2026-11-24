package com.example.base.modules.__domain__.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.base.common.PageResult;
import com.example.base.modules.__domain__.dto.__Domain__Query;
import com.example.base.modules.__domain__.entity.__Domain__;
import com.example.base.modules.__domain__.mapper.__Domain__Mapper;
import com.example.base.modules.__domain__.service.__Domain__Service;
import com.example.base.modules.__domain__.vo.__Domain__VO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * __title__业务层实现。
 *
 * ServiceImpl<__Domain__Mapper, __Domain__> 已实现通用 CRUD，
 * 继承后 this.page(...) / this.list(...) 可直接调用。
 */
@Service
public class __Domain__ServiceImpl extends ServiceImpl<__Domain__Mapper, __Domain__>
        implements __Domain__Service {

    /** DateTimeFormatter 线程安全可静态复用；SimpleDateFormat 不是，别混用 */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<__Domain__VO> query(__Domain__Query q) {
        // 1) 构造分页对象：MP 自动先 COUNT 再 LIMIT 查当前页
        //    依赖 core/MybatisPlusConfig 注册的分页插件，忘了配会返回全表且 total=0
        Page<__Domain__> page = Page.of(q.getPage(), q.getSize());

        // 2) Lambda 条件构造器：字段用方法引用，改名时编译期就能发现
        LambdaQueryWrapper<__Domain__> wrapper = Wrappers.lambdaQuery(__Domain__.class);

        String keyword = q.getKeyword();
        if (StringUtils.hasText(keyword)) {
            // ⚠️ 多个 OR 必须用 and(...) 包成括号整体，否则会与后面的 status 条件
            //    串成 (A OR B AND C)，SQL 优先级导致的逻辑错误很难查
            wrapper.and(w -> w.like(__Domain__::getName, keyword.trim())
                    .or().like(__Domain__::getName, keyword.trim()));
            // 生成后必改↑：把第二个 like 换成真实需要匹配的列（如 code / phone）
        }
        // 第一个参数是开关：false 时这段 SQL 完全不拼，省掉一堆 if
        wrapper.eq(q.getStatus() != null, __Domain__::getStatus, q.getStatus());
        wrapper.orderByDesc(__Domain__::getId);

        // 3) 执行查询；@TableLogic 会在此时自动追加 AND deleted = 0
        Page<__Domain__> result = this.page(page, wrapper);

        // 4) 实体 → VO 放 Service，Controller 只负责收发
        List<__Domain__VO> items = result.getRecords().stream().map(this::toVO).toList();

        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 单个实体转 VO：精度、字典、时间格式三类前端痛点一次处理掉 */
    private __Domain__VO toVO(__Domain__ e) {
        __Domain__VO v = new __Domain__VO();
        v.setId(String.valueOf(e.getId()));               // Long → String，防精度丢失
        v.setName(e.getName());
        v.setStatus(e.getStatus());
        v.setStatusLabel(Integer.valueOf(1).equals(e.getStatus()) ? "启用" : "停用");
        v.setCreatedAt(e.getCreatedAt() == null ? "" : e.getCreatedAt().format(FORMATTER));
        return v;
    }
}
