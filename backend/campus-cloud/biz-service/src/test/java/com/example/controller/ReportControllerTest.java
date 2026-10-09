package com.example.controller;

import com.example.constant.ReportConstant;
import com.example.dto.report.ReportCreateDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("举报模块 - Controller 层测试")
public class ReportControllerTest {

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

    // ==================== 提交举报 ====================

    @Test
    @DisplayName("提交举报 - 商品不存在")
    void testCreateProductNotFound() throws Exception {
        ReportCreateDTO dto = new ReportCreateDTO();
        dto.setTargetType(ReportConstant.TARGET_PRODUCT);
        dto.setTargetId(999999L);
        dto.setReason("虚假信息");

        mockMvc.perform(post("/user/reports")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("举报对象不存在"));
    }

    @Test
    @DisplayName("提交举报 - 举报自己的商品")
    void testCreateReportSelf() throws Exception {
        ReportCreateDTO dto = new ReportCreateDTO();
        dto.setTargetType(ReportConstant.TARGET_PRODUCT);
        dto.setTargetId(1001L);   // testuser001 自己的商品
        dto.setReason("测试");

        mockMvc.perform(post("/user/reports")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("不能举报自己"));
    }

    @Test
    @DisplayName("提交举报 - 缺参数")
    void testCreateMissingParam() throws Exception {
        ReportCreateDTO dto = new ReportCreateDTO();
        // 不设置 targetType

        mockMvc.perform(post("/user/reports")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("提交举报 - 未登录")
    void testCreateWithoutToken() throws Exception {
        ReportCreateDTO dto = new ReportCreateDTO();
        dto.setTargetType(ReportConstant.TARGET_PRODUCT);
        dto.setTargetId(1002L);
        dto.setReason("虚假信息");

        mockMvc.perform(post("/user/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 我的举报 ====================

    @Test
    @DisplayName("我的举报")
    void testListMyReports() throws Exception {
        mockMvc.perform(get("/user/reports")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    // ==================== 举报详情 ====================

    @Test
    @DisplayName("举报详情 - 不存在")
    void testDetailNotFound() throws Exception {
        mockMvc.perform(get("/user/reports/999999")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("举报不存在"));
    }

    // ==================== 提交申诉 ====================

    @Test
    @DisplayName("提交申诉 - 举报不存在")
    void testAppealReportNotFound() throws Exception {
        mockMvc.perform(post("/user/reports/999999/appeal")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"我是被冤枉的\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("举报不存在"));
    }
}