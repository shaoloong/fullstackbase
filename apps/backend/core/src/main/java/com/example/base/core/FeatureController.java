package com.example.base.core;

import com.example.base.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能目录接口：前端菜单的唯一数据源。
 *
 * 新增功能后，只要注册表里加了记录，菜单就会自动出现；
 * 前端不需要发版改硬编码（免构建预览页尤其依赖这一点）。
 *
 * 契约注意：@Tag 用英文（会变成 orval 生成的目录名），
 * 每个方法显式写 operationId（会变成前端的 hook / 函数名），别让框架猜。
 */
@Tag(name = "features", description = "功能目录")
@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureRegistry registry;

    @Operation(summary = "已注册功能列表", operationId = "listFeatures")
    @GetMapping
    public R<List<FeatureRegistry.Feature>> list() {
        return R.ok(registry.list());
    }
}
