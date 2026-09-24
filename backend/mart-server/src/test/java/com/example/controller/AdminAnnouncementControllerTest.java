package com.example.controller;

import com.example.dto.admin.AdminLoginDTO;
import com.example.dto.announcementManage.AnnouncementDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("管理端公告管理 - Controller 测试")
public class AdminAnnouncementControllerTest {

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
    @DisplayName("公告列表")
    void testListAnnouncements() throws Exception {
        mockMvc.perform(get("/admin/announcements")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("公告列表 - 未登录")
    void testListAnnouncementsWithoutToken() throws Exception {
        mockMvc.perform(get("/admin/announcements"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("发布公告")
    void testCreateAnnouncement() throws Exception {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setTitle("测试公告");
        dto.setContent("这是测试内容");
        dto.setStatus("published");

        MvcResult result = mockMvc.perform(post("/admin/announcements")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isString())        // ← 关键改动
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andReturn();

        String id = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").asText();
        assertThat(id).matches("\\d+");
    }

    @Test
    @DisplayName("发布公告 - 标题为空")
    void testCreateAnnouncementBlankTitle() throws Exception {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setTitle("");
        dto.setContent("内容");

        mockMvc.perform(post("/admin/announcements")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("修改公告 - 不存在")
    void testUpdateAnnouncementNotFound() throws Exception {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setTitle("修改后标题");
        dto.setContent("修改后内容");

        mockMvc.perform(put("/admin/announcements/999999")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("公告不存在"));
    }

    @Test
    @DisplayName("删除公告 - 不存在")
    void testDeleteAnnouncementNotFound() throws Exception {
        mockMvc.perform(delete("/admin/announcements/999999")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("公告不存在"));
    }

    @Test
    @DisplayName("删除公告 - 未登录")
    void testDeleteWithoutToken() throws Exception {
        mockMvc.perform(delete("/admin/announcements/1"))
                .andExpect(status().isUnauthorized());
    }
}