package com.example.controller;

import com.example.dto.admin.AdminLoginDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("管理端评价管理 - Controller 测试")
public class AdminReviewControllerTest {

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

    @Test
    @DisplayName("评价列表")
    void testListReviews() throws Exception {
        mockMvc.perform(get("/admin/reviews")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("评价列表 - 按关键词搜索")
    void testListReviewsByKeyword() throws Exception {
        mockMvc.perform(get("/admin/reviews")
                        .param("keyword", "好")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("评价列表 - 按评分筛选")
    void testListReviewsByScore() throws Exception {
        mockMvc.perform(get("/admin/reviews")
                        .param("score", "5")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("评价列表 - 未登录")
    void testListReviewsWithoutToken() throws Exception {
        mockMvc.perform(get("/admin/reviews"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("删除违规评价 - 不存在")
    void testDeleteReviewNotFound() throws Exception {
        mockMvc.perform(delete("/admin/reviews/999999")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("评价不存在"));
    }

    @Test
    @DisplayName("删除违规评价 - 未登录")
    void testDeleteReviewWithoutToken() throws Exception {
        mockMvc.perform(delete("/admin/reviews/1"))
                .andExpect(status().isUnauthorized());
    }
}