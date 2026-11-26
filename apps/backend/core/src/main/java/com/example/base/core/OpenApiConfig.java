package com.example.base.core;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 契约配置。
 *
 * 契约链路（对齐 tcmp，但用 Boot 3.5 可用的 springdoc 2.8.x）：
 *   Controller 注解 → /v3/api-docs → packages/api-client/openapi.json
 *   → scripts/gen-api.sh 生成 TS 类型 → 前端 import（不手写重复类型）
 *
 * ⚠️ info.version 必须与根 package.json 的 contractVersion 同步；
 *    scripts/gen-api.sh 会断言，不同步即拒绝生成。
 *    破坏性变更 bump MAJOR，兼容新增 bump MINOR。
 */
@Configuration
public class OpenApiConfig {

    /** 与根 package.json 的 contractVersion 保持一致；改这里时同步改那边 */
    public static final String CONTRACT_VERSION = "0.2.0";

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("fullstack-base API")
                .version(CONTRACT_VERSION)
                .description("可扩展全栈底座：前后端契约唯一真相，前端类型由此生成"));
    }
}
