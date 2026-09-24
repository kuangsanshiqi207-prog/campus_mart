package com.example.controller;

import com.example.dto.admin.AdminLoginDTO;
import com.example.dto.userManage.AdminUserQueryDTO;
import com.example.dto.userManage.CertificationAuditDTO;
import com.example.dto.userManage.CreditAdjustDTO;
import com.example.dto.userManage.UserStatusDTO;
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
@DisplayName("管理端用户管理 - Controller 测试")
public class AdminUserControllerTest {

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

    // ==================== 用户列表 ====================

    @Test
    @DisplayName("用户列表")
    void testListUsers() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("用户列表 - 关键词搜索")
    void testListUsersByKeyword() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("keyword", "test")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("用户列表 - 未登录 401")
    void testListUsersWithoutToken() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 用户详情 ====================

    @Test
    @DisplayName("用户详情 - 不存在")
    void testUserDetailNotFound() throws Exception {
        mockMvc.perform(get("/admin/users/999999")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("用户不存在"));
    }

    // ==================== 封禁 ====================

    @Test
    @DisplayName("封禁用户")
    void testBanUser() throws Exception {
        UserStatusDTO dto = new UserStatusDTO();
        dto.setStatus("banned");
        dto.setReason("测试封禁");

        mockMvc.perform(put("/admin/users/2/status")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("封禁用户 - 状态不合法")
    void testBanUserInvalidStatus() throws Exception {
        UserStatusDTO dto = new UserStatusDTO();
        dto.setStatus("invalid");

        mockMvc.perform(put("/admin/users/2/status")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("状态值不合法"));
    }

    // ==================== 信用调整 ====================

    @Test
    @DisplayName("调整信用分")
    void testAdjustCredit() throws Exception {
        CreditAdjustDTO dto = new CreditAdjustDTO();
        dto.setDelta(-10);
        dto.setReason("测试扣分");

        mockMvc.perform(put("/admin/users/2/credit")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    // ==================== 认证审核 ====================

    @Test
    @DisplayName("认证审核列表")
    void testListCertifications() throws Exception {
        mockMvc.perform(get("/admin/certifications")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("认证审核 - 不存在")
    void testAuditCertificationNotFound() throws Exception {
        CertificationAuditDTO dto = new CertificationAuditDTO();
        dto.setStatus("approved");

        mockMvc.perform(put("/admin/certifications/999999")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("认证记录不存在"));
    }
}