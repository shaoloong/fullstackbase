package com.example.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口（相当于前端项目里的 main.js / index.js）
 *
 * @SpringBootApplication 是三个注解的组合：
 *   ① @SpringBootConfiguration —— 标记这是配置类
 *   ② @EnableAutoConfiguration —— 依据 classpath 里的依赖自动装配
 *      例如发现 H2 驱动就配内存数据源，发现 Flyway 就自动执行迁移脚本
 *   ③ @ComponentScan          —— 扫描本包及子包下的 @Controller / @Service / @Mapper
 *
 * 注意：这个类必须放在最外层包（com.example.base），
 * 否则子包里的组件扫描不到，会出现「Bean 找不到」的经典报错。
 */
@SpringBootApplication
public class BaseApplication {

    public static void main(String[] args) {
        // 启动内嵌 Tomcat；端口由 application.yml 的 server.port 决定（本工程为 8082）
        SpringApplication.run(BaseApplication.class, args);
    }
}
