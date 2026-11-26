package com.example.base.modules.product.controller;

import com.example.base.common.PageResult;
import com.example.base.common.R;
import com.example.base.modules.product.dto.ProductCreateReq;
import com.example.base.modules.product.dto.ProductQuery;
import com.example.base.modules.product.dto.ProductUpdateReq;
import com.example.base.modules.product.service.ProductService;
import com.example.base.modules.product.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品管理接口层：接参数 → 调 Service → 包 R 响应，不写 SQL、不拼业务判断。
 *
 * 契约纪律：@Tag 用英文，每个方法显式写 operationId（前端生成函数名）。
 */
@Tag(name = "product", description = "产品管理")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** GET /api/products?page=1&size=10&keyword=xx&status=1 */
    @Operation(summary = "分页查询产品列表", operationId = "listProducts")
    @GetMapping
    public R<PageResult<ProductVO>> list(@Valid ProductQuery query) {
        return R.ok(productService.query(query));
    }

    /** GET /api/products/{id} */
    @Operation(summary = "查询产品详情", operationId = "getProduct")
    @GetMapping("/{id}")
    public R<ProductVO> get(@PathVariable String id) {
        return R.ok(productService.get(id));
    }

    /** POST /api/products */
    @Operation(summary = "新增产品", operationId = "createProduct")
    @PostMapping
    public R<String> create(@Valid @RequestBody ProductCreateReq req) {
        return R.ok(productService.create(req));
    }

    /** PUT /api/products/{id} */
    @Operation(summary = "修改产品", operationId = "updateProduct")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable String id,
                          @Valid @RequestBody ProductUpdateReq req) {
        productService.update(id, req);
        return R.ok(null);
    }

    /** DELETE /api/products/{id}：逻辑删除 */
    @Operation(summary = "删除产品（逻辑删除）", operationId = "deleteProduct")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return R.ok(null);
    }
}
