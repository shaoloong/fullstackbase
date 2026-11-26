package com.example.base.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.base.common.BizException;
import com.example.base.common.PageResult;
import com.example.base.modules.product.dto.ProductCreateReq;
import com.example.base.modules.product.dto.ProductQuery;
import com.example.base.modules.product.dto.ProductUpdateReq;
import com.example.base.modules.product.entity.Product;
import com.example.base.modules.product.mapper.ProductMapper;
import com.example.base.modules.product.service.ProductService;
import com.example.base.modules.product.vo.ProductVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 产品业务层实现：单表操作全部走 BaseMapper / LambdaQueryWrapper，零手写 SQL。
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    /** DateTimeFormatter 线程安全可静态复用；SimpleDateFormat 不是，别混用 */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<ProductVO> query(ProductQuery q) {
        // 1) 分页对象：MP 自动先 COUNT 再 LIMIT，依赖 core 里注册的分页插件
        Page<Product> page = Page.of(q.getPage(), q.getSize());

        LambdaQueryWrapper<Product> wrapper = Wrappers.lambdaQuery(Product.class);

        String keyword = q.getKeyword();
        if (StringUtils.hasText(keyword)) {
            String like = keyword.trim();
            // ⚠️ 两个 OR 必须用 and(...) 包成括号整体，否则会和后面的 status 串条件
            wrapper.and(w -> w.like(Product::getName, like)
                    .or().like(Product::getCode, like));
        }
        wrapper.eq(q.getStatus() != null, Product::getStatus, q.getStatus());
        wrapper.orderByDesc(Product::getId);

        // @TableLogic 会自动追加 AND deleted = 0
        Page<Product> result = this.page(page, wrapper);
        List<ProductVO> items = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ProductVO get(String id) {
        // 显式走 lambdaQuery 以继承逻辑删除过滤；不用 getById 避免语义差异
        Product product = this.lambdaQuery().eq(Product::getId, parseId(id)).one();
        if (product == null) {
            throw new BizException(404, "产品不存在");
        }
        return toVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(ProductCreateReq req) {
        String code = req.getCode() == null ? null : req.getCode().trim();
        // 服务层兜底：即使绕过 @Valid 直接调用，也不允许写入非法编码/状态
        validateCode(code);
        validateStatus(req.getStatus());
        // 应用层预检给出友好 409；数据库唯一约束兜住并发竞态
        if (this.lambdaQuery().eq(Product::getCode, code).exists()) {
            throw new BizException(409, "产品编码已存在");
        }

        Product entity = new Product();
        entity.setName(req.getName().trim());
        entity.setCode(code);
        entity.setCategory(req.getCategory());
        entity.setPrice(req.getPrice());
        entity.setStock(req.getStock());
        entity.setDescription(req.getDescription());
        entity.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        try {
            save(entity);
        } catch (DuplicateKeyException e) {
            throw new BizException(409, "产品编码已存在");
        }
        // created_at / updated_at 由 DB 默认值填充；返回 String 防前端精度丢失
        return String.valueOf(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String id, ProductUpdateReq req) {
        Long productId = parseId(id);
        Product existing = this.lambdaQuery().eq(Product::getId, productId).one();
        if (existing == null) {
            throw new BizException(404, "产品不存在");
        }
        if (!hasAnyField(req)) {
            throw new BizException(400, "没有可更新的字段");
        }

        // 仅覆盖 DTO 中非 null 的字段；id 与 created_at 保持原值
        // 服务层兜底：显式传空/纯空白 code 视为非法，绝不清空业务唯一键
        if (req.getCode() != null) {
            String newCode = req.getCode().trim();
            validateCode(newCode);
            if (!newCode.equals(existing.getCode())) {
                if (this.lambdaQuery().eq(Product::getCode, newCode).exists()) {
                    throw new BizException(409, "产品编码已存在");
                }
                existing.setCode(newCode);
            }
        }
        if (req.getName() != null) existing.setName(req.getName().trim());
        if (req.getCategory() != null) existing.setCategory(req.getCategory());
        if (req.getPrice() != null) existing.setPrice(req.getPrice());
        if (req.getStock() != null) existing.setStock(req.getStock());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        validateStatus(req.getStatus());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        // 没有 MetaObjectHandler，更新时间由服务层显式赋值
        existing.setUpdatedAt(LocalDateTime.now());
        try {
            updateById(existing);
        } catch (DuplicateKeyException e) {
            throw new BizException(409, "产品编码已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        // removeById 走逻辑删除：UPDATE deleted=1；已删/不存在的行影响 0 行 → 404
        if (!removeById(parseId(id))) {
            throw new BizException(404, "产品不存在");
        }
    }

    /** 路径 id 统一解析：非法格式返回 400，不把 NumberFormatException 漏成 500 */
    private Long parseId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BizException(400, "ID 格式不正确");
        }
        try {
            return Long.valueOf(id.trim());
        } catch (NumberFormatException e) {
            throw new BizException(400, "ID 格式不正确");
        }
    }

    private boolean hasAnyField(ProductUpdateReq req) {
        return req.getName() != null || req.getCode() != null || req.getCategory() != null
                || req.getPrice() != null || req.getStock() != null
                || req.getDescription() != null || req.getStatus() != null;
    }

    /** 编码是业务唯一键且必填：null / 空串 / 纯空白一律 400 */
    private void validateCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException(400, "产品编码不能为空");
        }
    }

    /** 状态领域取值只有 1 启用 / 0 停用：其余值一律 400（null 由调用方决定是否默认） */
    private void validateStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BizException(400, "状态只能是 0（停用）或 1（启用）");
        }
    }

    /** 实体 → VO：裁剪内部字段、id 转 String、补状态字典与时间格式 */
    private ProductVO toVO(Product e) {
        ProductVO v = new ProductVO();
        v.setId(String.valueOf(e.getId()));
        v.setName(e.getName());
        v.setCode(e.getCode());
        v.setCategory(e.getCategory());
        v.setPrice(e.getPrice());
        v.setStock(e.getStock());
        v.setDescription(e.getDescription());
        v.setStatus(e.getStatus());
        v.setStatusLabel(Integer.valueOf(1).equals(e.getStatus()) ? "启用" : "停用");
        v.setCreatedAt(e.getCreatedAt() == null ? "" : e.getCreatedAt().format(FORMATTER));
        return v;
    }
}
