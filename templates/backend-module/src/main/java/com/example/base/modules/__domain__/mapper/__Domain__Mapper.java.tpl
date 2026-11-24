package com.example.base.modules.__domain__.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.base.modules.__domain__.entity.__Domain__;
import org.apache.ibatis.annotations.Mapper;

/**
 * __title__数据访问层。
 *
 * 继承 BaseMapper 后单表 CRUD / 分页 / 批量插入全部现成，一行 SQL 不用写；
 * 只有联表或复杂统计才需要额外写 XML。
 *
 * @Mapper 让 Spring 生成代理对象；启动类在 com.example.base 根包是扫描生效的前提。
 */
@Mapper
public interface __Domain__Mapper extends BaseMapper<__Domain__> {
}
