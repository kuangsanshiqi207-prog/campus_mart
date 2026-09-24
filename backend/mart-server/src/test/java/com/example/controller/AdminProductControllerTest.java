package com.example.controller;

import com.example.dto.admin.AdminLoginDTO;
import com.example.dto.adminProduct.CategoryDTO;
import com.example.dto.adminProduct.OfflineDTO;
import com.example.dto.adminProduct.ProductAuditDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("管理端商品和分类管理 - Controller 测试")
public class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    public void setUp() throws Exception {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        String response = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("data").get("token").asText();
        adminToken = "Bearer " + token;
    }

    // ==================== 商品管理 ====================

    @Test
    @DisplayName("商品列表")
    void testListProducts() throws Exception {
        mockMvc.perform(get("/admin/products")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("商品列表 - 未登录")
    void testListProductsWithoutToken() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("商品详情 - 不存在")
    void testProductDetailNotFound() throws Exception {
        mockMvc.perform(get("/admin/products/999999")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("商品不存在"));
    }

    @Test
    @DisplayName("强制下架")
    void testForceOffline() throws Exception {
        OfflineDTO dto = new OfflineDTO();
        dto.setReason("测试下架");

        mockMvc.perform(put("/admin/products/1001/offline")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    // ==================== 分类管理 ====================

    @Test
    @DisplayName("分类列表")
    void testListCategories() throws Exception {
        mockMvc.perform(get("/admin/categories")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("新增分类")
    void testCreateCategory() throws Exception {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("测试分类");
        dto.setSort(99);
        // 如果 DTO 没有默认值，建议显式设置，避免依赖默认行为
        dto.setParentId(0L);
        dto.setStatus(1);

        MvcResult result = mockMvc.perform(post("/admin/categories")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isString())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andReturn();

        // 把返回的 id 提取出来，方便后续断言或清理
        String id = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").asText();
        assertThat(id).matches("\\d+");   // 纯数字字符串

    }

    @Test
    @DisplayName("删除分类 - 有商品关联")
    void testDeleteCategoryWithProducts() throws Exception {
        // 分类ID=1 下有商品，删除应失败
        mockMvc.perform(delete("/admin/categories/1")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("删除分类 - 不存在")
    void testDeleteCategoryNotFound() throws Exception {
        mockMvc.perform(delete("/admin/categories/999999")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("分类不存在"));
    }
}