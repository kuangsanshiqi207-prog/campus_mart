package com.example.controller;

import com.example.dto.user.LoginDTO;
import com.example.service.user.UserService;
import com.example.vo.user.LoginVO;
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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("评价模块 - Controller 层测试")
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("发表评价 - 订单不存在")
    void testCreateOrderNotFound() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", 999999);
        body.put("score", 5);

        mockMvc.perform(post("/user/reviews")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("订单不存在"));
    }

    @Test
    @DisplayName("发表评价 - 评分超范围")
    void testCreateScoreOutOfRange() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", 1);
        body.put("score", 10);

        mockMvc.perform(post("/user/reviews")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("评分最高 5 星"));
    }

    @Test
    @DisplayName("发表评价 - 缺参数")
    void testCreateMissingParam() throws Exception {
        Map<String, Object> body = new HashMap<>();
        // 不传 orderId 和 score

        mockMvc.perform(post("/user/reviews")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("我发出的评价")
    void testListMine() throws Exception {
        mockMvc.perform(get("/user/reviews/mine")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("我收到的评价")
    void testListReceived() throws Exception {
        mockMvc.perform(get("/user/reviews/received")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("商品评价列表")
    void testListByProduct() throws Exception {
        mockMvc.perform(get("/user/reviews/product/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("评价详情 - 不存在")
    void testDetailNotFound() throws Exception {
        mockMvc.perform(get("/user/reviews/detail/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("评价不存在"));
    }

    @Test
    @DisplayName("回复评价 - 评价不存在")
    void testReplyNotFound() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("content", "谢谢");

        mockMvc.perform(post("/user/reviews/999999/reply")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("评价不存在"));
    }

    @Test
    @DisplayName("未登录 - 401")
    void testWithoutToken() throws Exception {
        mockMvc.perform(get("/user/reviews/mine"))
                .andExpect(status().isUnauthorized());
    }
}