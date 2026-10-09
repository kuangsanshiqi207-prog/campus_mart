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
@DisplayName("管理端统计 - Controller 测试")
public class AdminStatsControllerTest {

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
    @DisplayName("首页概览")
    void testOverview() throws Exception {
        mockMvc.perform(get("/admin/stats/overview")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.userTotal").exists())
                .andExpect(jsonPath("$.data.productTotal").exists())
                .andExpect(jsonPath("$.data.orderTotal").exists());
    }

    @Test
    @DisplayName("首页概览 - 未登录")
    void testOverviewWithoutToken() throws Exception {
        mockMvc.perform(get("/admin/stats/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("用户统计趋势")
    void testUserTrend() throws Exception {
        mockMvc.perform(get("/admin/stats/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.dates").isArray())
                .andExpect(jsonPath("$.data.values").isArray());
    }

    @Test
    @DisplayName("用户统计趋势 - 指定日期范围")
    void testUserTrendWithRange() throws Exception {
        mockMvc.perform(get("/admin/stats/users")
                        .param("startDate", "2026-09-01")
                        .param("endDate", "2026-09-07")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.dates.length()").value(7));
    }

    @Test
    @DisplayName("商品统计趋势")
    void testProductTrend() throws Exception {
        mockMvc.perform(get("/admin/stats/products")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.dates").isArray());
    }

    @Test
    @DisplayName("订单统计趋势")
    void testOrderTrend() throws Exception {
        mockMvc.perform(get("/admin/stats/orders")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.dates").isArray());
    }

    @Test
    @DisplayName("AI审核统计")
    void testAuditStats() throws Exception {
        mockMvc.perform(get("/admin/stats/audits")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").exists());
    }

    @Test
    @DisplayName("举报统计")
    void testReportStats() throws Exception {
        mockMvc.perform(get("/admin/stats/reports")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.pending").exists());
    }

    @Test
    @DisplayName("导出统计 - 用户")
    void testExportUsers() throws Exception {
        mockMvc.perform(get("/admin/stats/export")
                        .param("type", "users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }
}