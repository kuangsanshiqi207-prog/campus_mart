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
@DisplayName("消息模块 - Controller 层测试")
public class MessageControllerTest {

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

    // ==================== 消息列表 ====================

    @Test
    @DisplayName("消息列表")
    void testListMessages() throws Exception {
        mockMvc.perform(get("/user/messages")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("消息列表 - 按类型筛选")
    void testListMessagesByType() throws Exception {
        mockMvc.perform(get("/user/messages")
                        .param("type", "order")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("消息列表 - 只看未读")
    void testListMessagesUnread() throws Exception {
        mockMvc.perform(get("/user/messages")
                        .param("isRead", "0")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    @DisplayName("消息列表 - 未登录 401")
    void testListWithoutToken() throws Exception {
        mockMvc.perform(get("/user/messages"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 未读数 ====================

    @Test
    @DisplayName("未读数")
    void testUnreadCount() throws Exception {
        mockMvc.perform(get("/user/messages/unread-count")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isString())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    // ==================== 标记已读 ====================

    @Test
    @DisplayName("标记已读 - 消息不存在")
    void testMarkReadNotFound() throws Exception {
        mockMvc.perform(put("/user/messages/999999/read")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("消息不存在"));
    }

    @Test
    @DisplayName("全部已读")
    void testMarkAllRead() throws Exception {
        mockMvc.perform(put("/user/messages/read-all")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    // ==================== 删除 ====================

    @Test
    @DisplayName("删除消息 - 消息不存在")
    void testDeleteNotFound() throws Exception {
        mockMvc.perform(delete("/user/messages/999999")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("消息不存在"));
    }

    // ==================== 公告 ====================

    @Test
    @DisplayName("公告列表 - 公开")
    void testListAnnouncements() throws Exception {
        mockMvc.perform(get("/user/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("公告详情 - 不存在")
    void testGetAnnouncementNotFound() throws Exception {
        mockMvc.perform(get("/user/announcements/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("公告不存在"));
    }
}