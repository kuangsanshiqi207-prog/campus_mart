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
class MarketControllerTest {

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

    // ==================== 公开接口 ====================

    @Test
    void testListProducts() throws Exception {
        mockMvc.perform(get("/user/market/products")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testListProductsByKeyword() throws Exception {
        mockMvc.perform(get("/user/market/products")
                        .param("keyword", "自行车"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testGetProductDetail() throws Exception {
        mockMvc.perform(get("/user/market/products/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1001))
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.images").isArray());
    }

    @Test
    void testGetProductDetailNotFound() throws Exception {
        mockMvc.perform(get("/user/market/products/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("商品不存在"));
    }

    @Test
    void testRecommend() throws Exception {
        mockMvc.perform(get("/user/market/products/recommend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testSimilar() throws Exception {
        mockMvc.perform(get("/user/market/products/1001/similar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetSeller() throws Exception {
        mockMvc.perform(get("/user/market/products/1001/seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.nickname").exists())
                .andExpect(jsonPath("$.data.creditScore").isNumber());
    }

    @Test
    void testListCategories() throws Exception {
        mockMvc.perform(get("/user/market/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").exists());
    }

    // ==================== 需登录接口 ====================

    @Test
    void testAddFavorite() throws Exception {
        mockMvc.perform(post("/user/market/favorites/1001")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testAddFavoriteWithoutToken() throws Exception {
        mockMvc.perform(post("/user/market/favorites/1001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddFavoriteDuplicate() throws Exception {
        mockMvc.perform(post("/user/market/favorites/1001")
                .header("Authorization", token));

        mockMvc.perform(post("/user/market/favorites/1001")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("已收藏该商品"));
    }

    @Test
    void testRemoveFavorite() throws Exception {
        mockMvc.perform(post("/user/market/favorites/1001")
                .header("Authorization", token));

        mockMvc.perform(delete("/user/market/favorites/1001")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testRemoveFavoriteNotExists() throws Exception {
        mockMvc.perform(delete("/user/market/favorites/1002")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("未收藏该商品"));
    }

    @Test
    void testListFavorites() throws Exception {
        mockMvc.perform(post("/user/market/favorites/1001")
                .header("Authorization", token));

        mockMvc.perform(get("/user/market/favorites")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void testListHistory() throws Exception {
        mockMvc.perform(get("/user/market/products/1001")
                .header("Authorization", token));

        mockMvc.perform(get("/user/market/history")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testClearHistory() throws Exception {
        mockMvc.perform(get("/user/market/products/1001")
                .header("Authorization", token));

        mockMvc.perform(delete("/user/market/history")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        // 清空后历史记录应为 0
        mockMvc.perform(get("/user/market/history")
                        .header("Authorization", token))
                .andExpect(jsonPath("$.data.total").value(0));
    }
}