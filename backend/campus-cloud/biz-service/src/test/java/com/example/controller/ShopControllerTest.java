package com.example.controller;

import com.example.dto.user.LoginDTO;
import com.example.service.user.UserService;
import com.example.vo.user.LoginVO;
import org.junit.jupiter.api.BeforeEach;
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
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private String token;

    @BeforeEach
    void setUp() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser001");
        dto.setPassword("123456");
        LoginVO vo = userService.login(dto);
        token = "Bearer " + vo.getToken();
    }

    @Test
    void testListMyProducts() throws Exception {
        mockMvc.perform(get("/user/shop/products")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testGetMyStats() throws Exception {
        mockMvc.perform(get("/user/shop/stats")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    void testGetMyProductDetail() throws Exception {
        mockMvc.perform(get("/user/shop/products/1001")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1001))
                .andExpect(jsonPath("$.data.title").value("二手自行车"));
    }

    @Test
    void testWithoutToken() throws Exception {
        mockMvc.perform(get("/user/shop/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testOfflineProduct() throws Exception {
        // 前置：先确保商品是上架状态（如果已是 on_sale，接口会返回 code=0，忽略即可）
        mockMvc.perform(put("/user/shop/products/1001/online")
                .header("Authorization", token));

        // 测试下线
        mockMvc.perform(put("/user/shop/products/1001/offline")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testOnlineProduct() throws Exception {
        // 前置：先确保商品是下线状态（如果已是 offline，接口会返回 code=0，忽略即可）
        mockMvc.perform(put("/user/shop/products/1001/offline")
                .header("Authorization", token));

        // 测试上线
        mockMvc.perform(put("/user/shop/products/1001/online")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }
}