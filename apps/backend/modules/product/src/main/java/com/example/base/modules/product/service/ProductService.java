package com.example.base.modules.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.base.common.PageResult;
import com.example.base.modules.product.dto.ProductCreateReq;
import com.example.base.modules.product.dto.ProductQuery;
import com.example.base.modules.product.dto.ProductUpdateReq;
import com.example.base.modules.product.entity.Product;
import com.example.base.modules.product.vo.ProductVO;

/**
 * 产品业务层接口：继承 IService 后通用 CRUD 直接可用，
 * 这里只声明需要拼装/校验的业务方法。
 */
public interface ProductService extends IService<Product> {

    /** 分页 + 关键词（name/code）+ 状态筛选 */
    PageResult<ProductVO> query(ProductQuery query);

    /** 详情；不存在或已删除时抛 404 */
    ProductVO get(String id);

    /** 新增；编码重复抛 409，成功返回新 id 的字符串 */
    String create(ProductCreateReq req);

    /** 修改；不存在 404 / 无更新字段 400 / 编码冲突 409 */
    void update(String id, ProductUpdateReq req);

    /** 逻辑删除；不存在或已删除时抛 404 */
    void delete(String id);
}
