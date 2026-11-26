package com.example.base.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.base.common.PageResult;
import com.example.base.modules.user.dto.UserQuery;
import com.example.base.modules.user.entity.User;
import com.example.base.modules.user.vo.UserVO;

import com.example.base.modules.user.dto.UserCreateReq;

/**
 * 业务层接口。
 *
 * 继承 MyBatis-Plus 的 IService 后，save / getById / page 等通用方法直接可用；
 * 这里只声明「通用方法覆盖不了」的业务查询，保持接口干净。
 */
public interface UserService extends IService<User> {

    /**
     * 分页 + 条件查询用户列表。
     *
     * @param query 页码、每页条数、关键词、状态
     * @return 前端约定形状的 { items, total, page, size }
     */
    PageResult<UserVO> query(UserQuery query);
    String create(UserCreateReq req);
}
