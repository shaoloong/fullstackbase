package com.example.base.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置。
 *
 * 本工程前端走 Vite 代理（浏览器只访问 5174，由 Vite 转发到 8082），其实不会产生跨域；
 * 但保留这层很有必要：一旦你直接用 8080/8082 的本地页面、或临时用 Apifox 之外的
 * 静态页面调后端，没有它就会被浏览器 CORS 策略拦下。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")                      // 只放开业务接口，别用 "/**" 图省事
                .allowedOriginPatterns("http://localhost:5174", "http://127.0.0.1:5174",
                                "http://localhost:8082", "http://127.0.0.1:8082")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);                              // 预检结果缓存 1 小时，减少 OPTIONS 请求
    }
}
