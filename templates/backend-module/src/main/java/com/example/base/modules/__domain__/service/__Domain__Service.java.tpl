package com.example.base.modules.__domain__.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.base.common.PageResult;
import com.example.base.modules.__domain__.dto.__Domain__Query;
import com.example.base.modules.__domain__.entity.__Domain__;
import com.example.base.modules.__domain__.vo.__Domain__VO;

/**
 * __title__业务层接口。
 *
 * 继承 IService 后 save / getById / page 等通用方法直接可用；
 * 这里只声明「通用方法覆盖不了」的查询，保持接口干净。
 * 后续新增「增 / 改 / 删」也在此加方法，由 ServiceImpl 实现。
 */
public interface __Domain__Service extends IService<__Domain__> {

    /** 分页 + 条件查询 */
    PageResult<__Domain__VO> query(__Domain__Query query);
}
