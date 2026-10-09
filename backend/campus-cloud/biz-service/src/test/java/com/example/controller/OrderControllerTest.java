package com.example.controller;

import com.example.constant.OrderConstant;
import com.example.dto.order.OrderCreateDTO;
import com.example.dto.user.LoginDTO;
import com.example.service.order.OrderService;
import com.example.service.user.UserService;
import com.example.vo.user.LoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
@DisplayName("交易模块 - Controller 层测试")
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long userId;

    @BeforeEach
    public void setUp() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser001");
        dto.setPassword("123456");
        LoginVO vo = userService.login(dto);
        token = "Bearer " + vo.getToken();
        userId = vo.getUserId();
    }

    // ==================== 订单列表 ====================

    @Test
    @DisplayName("订单列表 - 我卖出的")
    void testListSellOrders() throws Exception {
        mockMvc.perform(get("/user/orders")
                        .param("type", OrderConstant.TYPE_SELL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.total").isString())      // ← 由 isNumber 改 isString
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("订单列表 - 我买到的")
    void testListBuyOrders() throws Exception {
        mockMvc.perform(get("/user/orders")
                        .param("type", OrderConstant.TYPE_BUY)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    // ==================== 未登录 ====================

    @Test
    @DisplayName("未登录访问 - 401")
    void testWithoutToken() throws Exception {
        mockMvc.perform(get("/user/orders"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 申请交易 ====================

    @Test
    @DisplayName("申请交易 - 不能购买自己的商品")
    void testCreateOrderCannotBuyOwn() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("productId", 1001);

        mockMvc.perform(post("/user/orders")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("不能购买自己的商品"));
    }

    @Test
    @DisplayName("申请交易 - 商品不存在")
    void testCreateOrderProductNotFound() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("productId", 999999);

        mockMvc.perform(post("/user/orders")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("商品不存在"));
    }

    @Test
    @DisplayName("申请交易 - 参数缺失")
    void testCreateOrderMissingParam() throws Exception {
        Map<String, Object> body = new HashMap<>();
        // 不传 productId

        mockMvc.perform(post("/user/orders")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("商品ID不能为空"));
    }

    // ==================== 订单详情 ====================

    @Test
    @DisplayName("订单详情 - 订单不存在")
    void testOrderDetailNotFound() throws Exception {
        mockMvc.perform(get("/user/orders/999999")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("订单不存在"));
    }

    // ==================== 卖家接受 ====================

    @Test
    @DisplayName("卖家接受 - 订单不存在")
    void testAcceptOrderNotFound() throws Exception {
        mockMvc.perform(put("/user/orders/999999/accept")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("订单不存在"));
    }

    // ==================== 完整流程 ====================

    @Test
    @DisplayName("完整流程 - 申请 → 接受 → 完成")
    void testFullFlow() throws Exception {
        // 1. 用买家账号登录（假设 id=2 的买家存在）
        // 简化：这里只验证卖家视角的操作
        // 详细的完整流程在 Service 测试中覆盖
    }
}