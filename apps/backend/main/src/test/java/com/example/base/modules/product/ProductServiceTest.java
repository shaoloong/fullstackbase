package com.example.base.modules.product;

import com.example.base.common.BizException;
import com.example.base.modules.product.dto.ProductCreateReq;
import com.example.base.modules.product.dto.ProductQuery;
import com.example.base.modules.product.dto.ProductUpdateReq;
import com.example.base.modules.product.service.ProductService;
import com.example.base.modules.product.vo.ProductVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 产品管理集成测试：Service 层断言 + MockMvc 走完整 Controller 链路。
 *
 * @Transactional 让每个用例在事务中回滚，不污染 H2 种子数据（用户 total=23 断言不受影响）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /** 两条未删除 + 一条已逻辑删除的产品，覆盖逻辑删除过滤场景 */
    @BeforeEach
    void seed() {
        jdbcTemplate.update("DELETE FROM t_product");
        insert(901, "测试产品甲", "TS001", "电子", "10.00", 5, 1, 0);
        insert(902, "测试产品乙", "TS002", "日用品", "20.00", 0, 0, 0);
        insert(903, "已删产品", "TS003", "电子", "30.00", 1, 1, 1);
    }

    private void insert(int id, String name, String code, String category,
                        String price, int stock, int status, int deleted) {
        jdbcTemplate.update(
                "INSERT INTO t_product (id, name, code, category, price, stock, description, "
                        + "status, deleted, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                id, name, code, category, new BigDecimal(price), stock, "描述-" + code,
                status, deleted, Timestamp.valueOf("2026-01-01 10:00:00"),
                Timestamp.valueOf("2026-01-01 10:00:00"));
    }

    private ProductCreateReq createReq(String name, String code, String price, int stock) {
        ProductCreateReq req = new ProductCreateReq();
        req.setName(name);
        req.setCode(code);
        req.setPrice(new BigDecimal(price));
        req.setStock(stock);
        return req;
    }

    // ---------- F1 列表 ----------

    @Test
    void query_shouldPageExcludeDeletedAndUseStringId() {
        ProductQuery q = new ProductQuery();
        q.setPage(1);
        q.setSize(1);
        var result = productService.query(q);

        assertEquals(2, result.getTotal());          // 3 条种子中 1 条已逻辑删除
        assertEquals(1, result.getItems().size());
        ProductVO first = result.getItems().get(0);
        assertEquals("902", first.getId());          // id DESC
        assertTrue(first.getId() instanceof String);
        assertEquals("TS002", first.getCode());
        assertEquals(new BigDecimal("20.00"), first.getPrice());
        assertEquals("停用", first.getStatusLabel());
    }

    @Test
    void query_keywordMatchesNameOrCodeAndStatusDoesNotCrossConditions() {
        ProductQuery byCode = new ProductQuery();
        byCode.setKeyword("TS002");
        assertEquals(1, productService.query(byCode).getTotal());

        ProductQuery byName = new ProductQuery();
        byName.setKeyword("产品甲");
        assertEquals(1, productService.query(byName).getTotal());

        // keyword 命中两条（甲/乙），status=0 应只保留乙：OR 必须用 and(...) 包整体
        ProductQuery combined = new ProductQuery();
        combined.setKeyword("测试");
        combined.setStatus(0);
        combined.setSize(100);
        var result = productService.query(combined);
        assertEquals(1, result.getTotal());
        assertEquals("902", result.getItems().get(0).getId());
        assertTrue(result.getItems().stream().allMatch(v -> v.getStatus() == 0));
    }

    // ---------- F3 新增 / 唯一性 ----------

    @Test
    void create_thenGet_shouldReturnSameFieldsAndRejectDuplicateCode() {
        String id = productService.create(createReq("新品", "TS100", "12.34", 7));
        assertNotNull(Long.valueOf(id));

        ProductVO got = productService.get(id);
        assertEquals("新品", got.getName());
        assertEquals("TS100", got.getCode());
        assertEquals(new BigDecimal("12.34"), got.getPrice());
        assertEquals(7, got.getStock());
        assertEquals(1, got.getStatus());            // 未传 status 默认启用
        assertEquals("启用", got.getStatusLabel());
        assertNotNull(got.getCreatedAt());

        BizException ex = assertThrows(BizException.class,
                () -> productService.create(createReq("另一个", "TS100", "1.00", 1)));
        assertEquals(409, ex.getCode());
    }

    // ---------- F4 修改 ----------

    @Test
    void update_shouldKeepIdAndCreatedAtAndRejectMissingOrDuplicate() {
        String id = productService.create(createReq("待改产品", "TS200", "10.00", 1));
        ProductVO before = productService.get(id);

        ProductUpdateReq req = new ProductUpdateReq();
        req.setName("改名产品");
        req.setPrice(new BigDecimal("88.80"));
        productService.update(id, req);

        ProductVO after = productService.get(id);
        assertEquals("改名产品", after.getName());
        assertEquals(new BigDecimal("88.80"), after.getPrice());
        assertEquals(before.getId(), after.getId());
        assertEquals(before.getCreatedAt(), after.getCreatedAt());
        assertEquals("TS200", after.getCode());       // 未传字段保持原值

        assertEquals(404, assertThrows(BizException.class,
                () -> productService.update("999999", req)).getCode());
        assertEquals(400, assertThrows(BizException.class,
                () -> productService.update(id, new ProductUpdateReq())).getCode());

        ProductUpdateReq conflict = new ProductUpdateReq();
        conflict.setCode("TS001");
        assertEquals(409, assertThrows(BizException.class,
                () -> productService.update(id, conflict)).getCode());
    }

    // ---------- F5 逻辑删除 ----------

    @Test
    void delete_shouldBeLogicalAndGetReturns404() {
        String id = productService.create(createReq("待删产品", "TS300", "5.00", 2));
        long before = productService.query(new ProductQuery()).getTotal();

        productService.delete(id);
        assertEquals(404, assertThrows(BizException.class, () -> productService.get(id)).getCode());
        assertEquals(before - 1, productService.query(new ProductQuery()).getTotal());

        Integer deleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM t_product WHERE id = ?", Integer.class, Long.valueOf(id));
        assertEquals(1, deleted);                    // 数据行仍在库里，仅标记删除
        assertEquals(404, assertThrows(BizException.class, () -> productService.delete(id)).getCode());
    }

    // ---------- MockMvc：参数校验 / 分页越界 / 完整 HTTP CRUD ----------

    @Test
    void mockMvc_shouldValidateParametersAndReturnBusinessCodes() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"TS400\",\"price\":1,\"stock\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"价格非法\",\"code\":\"TS401\",\"price\":-1,\"stock\":1}"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"库存非法\",\"code\":\"TS402\",\"price\":1,\"stock\":-1}"))
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(get("/api/products?page=0"))
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(put("/api/products/999999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":1}"))
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(delete("/api/products/999999"))
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"编码冲突\",\"code\":\"TS001\",\"price\":1,\"stock\":1}"))
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void mockMvc_fullCrudFlow_shouldWorkEndToEnd() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"HTTP产品\",\"code\":\"TS500\","
                                + "\"price\":9.99,\"stock\":3,\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode body = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = body.get("data").asText();
        assertNotEquals("", id);

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("HTTP产品"))
                .andExpect(jsonPath("$.data.id").value(id));

        mockMvc.perform(get("/api/products").param("keyword", "TS500"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":19.99}"))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(jsonPath("$.data.price").value(19.99));

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(jsonPath("$.code").value(404));
    }

    // ---------- 评审 D1 回归：空编码 / 非法状态必须 400，且原记录不被改写 ----------

    @Test
    void mockMvc_shouldRejectBlankCodeAndIllegalStatusAndKeepRecord() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"校验基线\",\"code\":\"VLD001\","
                                + "\"price\":1.00,\"stock\":1,\"status\":1}"))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("data").asText();

        // POST：非法状态
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"非法状态\",\"code\":\"VLD002\","
                                + "\"price\":1,\"stock\":1,\"status\":99}"))
                .andExpect(jsonPath("$.code").value(400));
        // POST：纯空白编码
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"空白编码\",\"code\":\"   \","
                                + "\"price\":1,\"stock\":1}"))
                .andExpect(jsonPath("$.code").value(400));

        // PUT：空编码 / 纯空白编码 / 非法状态
        mockMvc.perform(put("/api/products/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\"}"))
                .andExpect(jsonPath("$.code").value(400));
        mockMvc.perform(put("/api/products/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"   \"}"))
                .andExpect(jsonPath("$.code").value(400));
        mockMvc.perform(put("/api/products/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":99}"))
                .andExpect(jsonPath("$.code").value(400));

        // 每一次失败请求后原记录都保持原值（业务唯一键未被清空、无幽灵状态）
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(jsonPath("$.data.code").value("VLD001"))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    void serviceLayer_shouldRejectBlankCodeAndIllegalStatusWithoutBeanValidation() {
        assertEquals(400, assertThrows(BizException.class,
                () -> productService.create(createReq("空编码", "", "1.00", 1))).getCode());
        assertEquals(400, assertThrows(BizException.class,
                () -> productService.create(createReq("空白编码", "   ", "1.00", 1))).getCode());

        ProductCreateReq createStatus = createReq("非法状态", "VLD100", "1.00", 1);
        createStatus.setStatus(99);
        assertEquals(400, assertThrows(BizException.class,
                () -> productService.create(createStatus)).getCode());

        String id = productService.create(createReq("兜底正常", "VLD101", "1.00", 1));

        ProductUpdateReq blankCode = new ProductUpdateReq();
        blankCode.setCode("");
        assertEquals(400, assertThrows(BizException.class,
                () -> productService.update(id, blankCode)).getCode());

        ProductUpdateReq spaceCode = new ProductUpdateReq();
        spaceCode.setCode("   ");
        assertEquals(400, assertThrows(BizException.class,
                () -> productService.update(id, spaceCode)).getCode());

        ProductUpdateReq badStatus = new ProductUpdateReq();
        badStatus.setStatus(99);
        assertEquals(400, assertThrows(BizException.class,
                () -> productService.update(id, badStatus)).getCode());

        ProductVO after = productService.get(id);
        assertEquals("VLD101", after.getCode());
        assertEquals(1, after.getStatus());
    }

    // ---------- 评审 D2 回归：编辑可清空可空字段 ----------

    @Test
    void mockMvc_editCanClearNullableFields() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"清空探测\",\"code\":\"CLR001\","
                                + "\"category\":\"旧分类\",\"description\":\"旧描述\","
                                + "\"price\":8.00,\"stock\":2,\"status\":1}"))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("data").asText();

        // 模拟前端 buildProductPayload：清空后提交空串（不是 null）
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"清空探测\",\"code\":\"CLR001\","
                                + "\"category\":\"\",\"description\":\"\","
                                + "\"price\":8.00,\"stock\":2,\"status\":1}"))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(jsonPath("$.data.category").value(""))
                .andExpect(jsonPath("$.data.description").value(""));
    }

    // ---------- AC10 点击级闭环的等效自动化证据（环境无浏览器） ----------

    @Test
    void mockMvc_ac10FullFlow_shouldCloseLoopEndToEnd() throws Exception {
        // AC10-1 新增
        MvcResult created = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"AC10闭环\",\"code\":\"AC10001\","
                                + "\"category\":\"电子\",\"price\":10.00,"
                                + "\"stock\":5,\"status\":1}"))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("data").asText();

        // AC10-2 搜索命中
        mockMvc.perform(get("/api/products").param("keyword", "AC10001"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(id));

        // AC10-3 编辑改价
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"AC10闭环\",\"code\":\"AC10001\","
                                + "\"category\":\"电子\",\"price\":18.88,"
                                + "\"stock\":5,\"status\":1}"))
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(jsonPath("$.data.price").value(18.88));

        // AC10-4 删除
        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(jsonPath("$.code").value(0));

        // AC10-5 列表不再出现；刷新详情 404
        mockMvc.perform(get("/api/products").param("keyword", "AC10001"))
                .andExpect(jsonPath("$.data.total").value(0));
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(jsonPath("$.code").value(404));
    }
}
