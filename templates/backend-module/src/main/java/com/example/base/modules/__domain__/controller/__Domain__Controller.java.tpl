package com.example.base.modules.__domain__.controller;

import com.example.base.common.PageResult;
import com.example.base.common.R;
import com.example.base.modules.__domain__.dto.__Domain__Query;
import com.example.base.modules.__domain__.service.__Domain__Service;
import com.example.base.modules.__domain__.vo.__Domain__VO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * __title__接口层。
 *
 * 分层职责：Controller 只做「接参数 → 调 Service → 包响应」，不写 SQL、不拼业务判断。
 * 后续加 POST / PUT / DELETE 时沿用同一套 R 外壳与 @Operation 契约注解。
 *
 * 契约纪律：@Tag 用英文（会成为 orval 生成的目录名），
 * 每个方法显式写 operationId（会成为前端函数名），别让框架猜。
 */
@Tag(name = "__domain__", description = "__title__管理")
@RestController
@RequestMapping("__api__")
@RequiredArgsConstructor   // Lombok 为 final 字段生成构造器，即推荐的构造器注入
public class __Domain__Controller {

    private final __Domain__Service __domain__Service;

    /** GET __api__?page=1&size=10&keyword=xx&status=1 */
    @Operation(summary = "分页查询__title__列表", operationId = "list__Domain__s")
    @GetMapping
    public R<PageResult<__Domain__VO>> list(@Valid __Domain__Query query) {
        return R.ok(__domain__Service.query(query));
    }
}
