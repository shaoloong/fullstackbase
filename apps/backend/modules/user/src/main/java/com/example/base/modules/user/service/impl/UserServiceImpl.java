package com.example.base.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.base.common.PageResult;
import com.example.base.modules.user.dto.UserQuery;
import com.example.base.modules.user.entity.User;
import com.example.base.modules.user.mapper.UserMapper;
import com.example.base.modules.user.service.UserService;
import com.example.base.modules.user.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.base.common.BizException;
import com.example.base.modules.user.dto.UserCreateReq;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务层实现。
 *
 * ServiceImpl<UserMapper, User> 已经把通用 CRUD 实现好了，
 * 继承它之后 this.page(...) / this.list(...) 可以直接调用。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    @Transactional
    public String create(UserCreateReq req) {
        // TODO: 实现用户创建逻辑
        return null;
    }
    /** DateTimeFormatter 线程安全，可以静态复用；SimpleDateFormat 不是，别混用 */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<UserVO> query(UserQuery q) {
        // 1) 构造分页对象：MP 会自动先 COUNT 再 LIMIT 查当前页
        //    依赖 MybatisPlusConfig 里注册的分页插件，忘了配就会返回全表数据
        Page<User> page = Page.of(q.getPage(), q.getSize());

        // 2) 用 Lambda 条件构造器拼 WHERE：字段用方法引用，改名时编译期就能发现，
        //    比手写字符串 "name" 安全得多
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class);

        String keyword = q.getKeyword();
        if (StringUtils.hasText(keyword)) {
            String like = keyword.trim();
            // ⚠️ 三个 OR 必须用 and(...) 包成一个括号整体，
            //    否则会和后面的 status 条件串成 (A OR B OR C AND D)，逻辑就错了
            wrapper.and(w -> w.like(User::getName, like)
                    .or().like(User::getPhone, like)
                    .or().like(User::getEmail, like));
        }
        // 第一个参数是「开关」：条件为 false 时这段 SQL 完全不拼，省掉一堆 if
        wrapper.eq(q.getStatus() != null, User::getStatus, q.getStatus());
        wrapper.orderByDesc(User::getId);

        // 3) 执行查询；@TableLogic 会在此时自动追加 AND deleted = 0
        Page<User> result = this.page(page, wrapper);

        // 4) 实体 → VO：批量转换放在 Service 里，Controller 只负责收发，不碰业务细节
        List<UserVO> items = result.getRecords().stream().map(this::toVO).toList();

        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /** 单个实体转 VO；把「精度、字典、时间格式」三类前端痛点一次处理掉 */
    private UserVO toVO(User u) {
        UserVO v = new UserVO();
        v.setId(String.valueOf(u.getId()));          // Long → String，防前端精度丢失
        v.setName(u.getName());
        v.setPhone(u.getPhone());
        v.setEmail(u.getEmail());
        v.setStatus(u.getStatus());
        v.setStatusLabel(Integer.valueOf(1).equals(u.getStatus()) ? "启用" : "停用");
        v.setCreatedAt(u.getCreatedAt() == null ? "" : u.getCreatedAt().format(FORMATTER));
        return v;
    }
}
