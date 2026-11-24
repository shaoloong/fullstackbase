package com.example.base.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.base.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper = 数据访问层（前端类比：封装好的 dao / repository）。
 *
 * 继承 BaseMapper 后，单表 CRUD、分页、批量插入等方法全部现成，
 * 一行 SQL 都不用写；只有当联表或复杂统计时才需要写 XML。
 *
 * @Mapper 让 Spring 扫描并生成代理对象；启动类放在最外层包是扫描生效的前提。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
