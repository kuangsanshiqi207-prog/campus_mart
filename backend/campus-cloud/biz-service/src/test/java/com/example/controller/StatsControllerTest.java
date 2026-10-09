package com.example.controller;

import com.example.dto.user.LoginDTO;
import com.example.service.user.UserService;
import com.example.vo.user.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("个人统计模块 - Controller 层测试")
public class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private String token;

    @BeforeEach
    public void setUp() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser001");
        dto.setPassword("123456");
        LoginVO vo = userService.login(dto);
        token = "Bearer " + vo.getToken();
    }

    @Test
    @DisplayName("数据概览")
    void testOverview() throws Exception {
        mockMvc.perform(get("/user/stats/overview")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.productTotal").isNumber())
                .andExpect(jsonPath("$.data.creditScore").isNumber());
    }

    @Test
    @DisplayName("商品统计")
    void testShopStats() throws Exception {
        mockMvc.perform(get("/user/stats/shop")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("交易统计")
    void testOrderStats() throws Exception {
        mockMvc.perform(get("/user/stats/orders")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.buyTotal").isNumber());
    }

    @Test
    @DisplayName("评价统计")
    void testReviewStats() throws Exception {
        mockMvc.perform(get("/user/stats/reviews")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.receivedTotal").isNumber());
    }

    @Test
    @DisplayName("信用分流水")
    void testCreditLogs() throws Exception {
        mockMvc.perform(get("/user/stats/credit")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("信用分趋势")
    void testCreditTrend() throws Exception {
        mockMvc.perform(get("/user/stats/credit/trend")
                        .param("days", "30")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.dates").isArray())
                .andExpect(jsonPath("$.data.values").isArray());
    }

    @Test
    @DisplayName("交易趋势")
    void testOrderTrend() throws Exception {
        mockMvc.perform(get("/user/stats/orders/trend")
                        .param("type", "buy")
                        .param("days", "30")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.dates").isArray());
    }

    @Test
    @DisplayName("未登录")
    void testWithoutToken() throws Exception {
        mockMvc.perform(get("/user/stats/overview"))
                .andExpect(status().isUnauthorized());
    }
}