package com.example.base.core;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 *
 * ⚠️ 分页插件必须显式注册：不配的话 Page 对象传进去也会被当成普通查询，
 *    返回全部数据且 total 恒为 0 —— 新手最常踩的坑之一。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 指定 MySQL 方言：分页会生成 LIMIT ?,? 语法
        // 默认 H2 库是 MODE=MySQL，同样能执行该语法，所以一套配置兼容两个 profile
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setOverflow(false);   // 页码超过总页数时返回空列表，而不是偷偷回到第一页
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
