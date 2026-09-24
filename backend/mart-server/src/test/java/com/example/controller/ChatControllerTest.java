package com.example.controller;

import com.example.dto.chat.ConversationCreateDTO;
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
@DisplayName("私信模块 - Controller 层测试")
public class ChatControllerTest {

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
    @DisplayName("会话列表")
    void testListConversations() throws Exception {
        mockMvc.perform(get("/user/chat/conversations")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("创建会话 - 不能给自己发")
    void testCreateSelf() throws Exception {
        ConversationCreateDTO dto = new ConversationCreateDTO();
        dto.setTargetUserId(1L);   // 自己

        mockMvc.perform(post("/user/chat/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("不能给自己发消息"));
    }

    @Test
    @DisplayName("创建会话 - 对方不存在")
    void testCreateTargetNotFound() throws Exception {
        ConversationCreateDTO dto = new ConversationCreateDTO();
        dto.setTargetUserId(999999L);

        mockMvc.perform(post("/user/chat/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("对方用户不存在"));
    }

    @Test
    @DisplayName("历史消息 - 会话不存在")
    void testMessagesNotFound() throws Exception {
        mockMvc.perform(get("/user/chat/conversations/999999/messages")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("会话不存在"));
    }

    @Test
    @DisplayName("标记已读 - 会话不存在")
    void testMarkReadNotFound() throws Exception {
        mockMvc.perform(put("/user/chat/conversations/999999/read")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("会话不存在"));
    }

    @Test
    @DisplayName("未登录")
    void testWithoutToken() throws Exception {
        mockMvc.perform(get("/user/chat/conversations"))
                .andExpect(status().isUnauthorized());
    }
}