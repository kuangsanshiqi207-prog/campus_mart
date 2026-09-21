package com.example.controller;

import com.example.dto.user.LoginDTO;
import com.example.dto.user.RegisterDTO;
import com.example.service.UserService;
import com.example.vo.user.LoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // ========== 登录接口 ==========
    @Test
    void testLoginApi() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        LoginVO vo = LoginVO.builder()
                .token("mock-token")
                .userId(1L)
                .username("testuser")
                .nickname("用户123456")
                .role("USER")
                .build();

        when(userService.login(any(LoginDTO.class))).thenReturn(vo);

        mockMvc.perform(post("/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.token").value("mock-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    // ========== 注册接口 ==========
    @Test
    void testRegisterApi() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");
        dto.setPhone("13800138000");
        dto.setCode("123456");

        when(userService.register(any(RegisterDTO.class))).thenReturn(1L);

        mockMvc.perform(post("/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    // ========== 参数校验失败 ==========
    @Test
    void testRegisterInvalidPhone() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");
        dto.setPhone("123");       // 手机号格式错误
        dto.setCode("123456");

        mockMvc.perform(post("/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ========== 发送验证码 ==========
    @Test
    void testSendCodeApi() throws Exception {
        mockMvc.perform(post("/user/auth/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }
}