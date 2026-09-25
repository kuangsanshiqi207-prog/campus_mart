package com.example.controller;

import com.example.dto.admin.AdminLoginDTO;
import com.example.dto.reportManage.AppealHandleDTO;
import com.example.dto.reportManage.ReportHandleDTO;
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
@DisplayName("管理端举报管理 - Controller 测试")
public class AdminReportControllerTest {

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

    // ==================== 举报 ====================

    @Test
    @DisplayName("举报列表")
    void testListReports() throws Exception {
        mockMvc.perform(get("/admin/reports")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("举报列表 - 按状态筛选")
    void testListReportsByStatus() throws Exception {
        mockMvc.perform(get("/admin/reports")
                        .param("status", "pending")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("举报列表 - 未登录")
    void testListReportsWithoutToken() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("举报详情 - 不存在")
    void testReportDetailNotFound() throws Exception {
        mockMvc.perform(get("/admin/reports/999999")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("举报不存在"));
    }

    @Test
    @DisplayName("处理举报 - 不存在")
    void testHandleReportNotFound() throws Exception {
        ReportHandleDTO dto = new ReportHandleDTO();
        dto.setResult("valid");
        dto.setAction("none");
        dto.setReason("测试");

        mockMvc.perform(put("/admin/reports/999999/handle")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("举报不存在"));
    }

    @Test
    @DisplayName("处理举报 - 结果不合法")
    void testHandleReportInvalidResult() throws Exception {
        ReportHandleDTO dto = new ReportHandleDTO();
        dto.setResult("xxx");

        mockMvc.perform(put("/admin/reports/1/handle")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("处理举报 - 结果为空")
    void testHandleReportBlankResult() throws Exception {
        ReportHandleDTO dto = new ReportHandleDTO();
        dto.setResult("");

        mockMvc.perform(put("/admin/reports/1/handle")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 申诉 ====================

    @Test
    @DisplayName("申诉列表")
    void testListAppeals() throws Exception {
        mockMvc.perform(get("/admin/appeals")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("处理申诉 - 不存在")
    void testHandleAppealNotFound() throws Exception {
        AppealHandleDTO dto = new AppealHandleDTO();
        dto.setResult("approve");
        dto.setReason("测试");

        mockMvc.perform(put("/admin/appeals/999999/handle")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("申诉不存在"));
    }

    @Test
    @DisplayName("处理申诉 - 结果不合法")
    void testHandleAppealInvalidResult() throws Exception {
        AppealHandleDTO dto = new AppealHandleDTO();
        dto.setResult("xxx");

        mockMvc.perform(put("/admin/appeals/1/handle")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}