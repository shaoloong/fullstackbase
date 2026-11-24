package com.example.base.modules.__domain__;

import com.example.base.modules.__domain__.dto.__Domain__Query;
import com.example.base.modules.__domain__.service.__Domain__Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * __title__模块的集成测试骨架（放在本模块 src/test，随 mvn test 执行）。
 *
 * ⚠️ 命名必须以 Test 结尾：surefire 默认只跑 *Test，写成 *IT 会被静默跳过。
 * 复杂 Service 纯单测可用 Mockito mock Mapper，不需要起 Spring 容器，速度更快。
 */
@SpringBootTest
class __Domain__ServiceTest {

    @Autowired
    private __Domain__Service __domain__Service;

    @Test
    void query_shouldReturnPageResult() {
        __Domain__Query q = new __Domain__Query();
        q.setPage(1);
        q.setSize(10);
        assertNotNull(__domain__Service.query(q));
    }
}
