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
@DisplayName("管理端认证模块 - Controller 层测试")
public class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    public void setUp() throws Exception {
        // 每次测试前先登录拿 admin token
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

    // ==================== 登录 ====================

    @Test
    @DisplayName("登录成功")
    void testLoginSuccess() throws Exception {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("登录失败 - 用户名不存在")
    void testLoginUserNotFound() throws Exception {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("notexist");
        dto.setPassword("123456");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginWrongPassword() throws Exception {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrongpassword");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("登录失败 - 参数为空")
    void testLoginMissingParam() throws Exception {
        AdminLoginDTO dto = new AdminLoginDTO();
        // username 和 password 都不设置

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 当前管理员信息 ====================

    @Test
    @DisplayName("获取当前管理员信息")
    void testGetProfile() throws Exception {
        mockMvc.perform(get("/admin/auth/profile")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("获取当前管理员信息 - 未登录 401")
    void testGetProfileWithoutToken() throws Exception {
        mockMvc.perform(get("/admin/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 登出 ====================

    @Test
    @DisplayName("登出成功")
    void testLogout() throws Exception {
        mockMvc.perform(post("/admin/auth/logout")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("登出后原 token 失效")
    void testLogoutThenTokenInvalid() throws Exception {
        // 先登出
        mockMvc.perform(post("/admin/auth/logout")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        // 再用同一个 token 访问
        mockMvc.perform(get("/admin/auth/profile")
                        .header("Authorization", adminToken))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 权限校验 ====================

    @Test
    @DisplayName("用户 token 访问管理端接口 - 401 或 403")
    void testUserTokenAccessAdmin() throws Exception {
        // 用用户端 token 访问管理端接口
        // 这里简化处理，用一个格式合法但 type 不是 admin 的 token 测试
        // 实际生产环境需要构造一个用户 token
        // 这里先留空，等用户端登录流程完整后补充
    }
}