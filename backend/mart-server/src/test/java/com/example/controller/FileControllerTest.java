package com.example.controller;

import com.example.dto.user.LoginDTO;
import com.example.service.FileStorageService;
import com.example.service.user.UserService;
import com.example.vo.user.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @MockitoBean
    private FileStorageService fileStorageService;

    private String token;

    @BeforeEach
    public void setUp() {
        // 登录拿 token
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser001");
        dto.setPassword("123456");
        LoginVO vo = userService.login(dto);
        token = "Bearer " + vo.getToken();

        // Mock 上传
        when(fileStorageService.upload(any(), anyString()))
                .thenAnswer(invocation -> {
                    String fileName = invocation.getArgument(1);
                    return "https://mock.oss.com/" + fileName;
                });
    }

    // ==================== 上传成功 ====================

    @Test
    public void testUploadSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/user/files/upload")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.fileId").isNumber())
                .andExpect(jsonPath("$.data.url").exists())
                .andExpect(jsonPath("$.data.name").value("test.jpg"));
    }

    // ==================== 未登录 ====================

    @Test
    public void testUploadWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/user/files/upload")
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 空文件 ====================

    @Test
    public void testUploadEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/user/files/upload")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("文件不能为空"));
    }

    // ==================== 超过大小 ====================

    @Test
    public void testUploadTooLarge() throws Exception {
        byte[] bigContent = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "big.jpg",
                "image/jpeg",
                bigContent
        );

        mockMvc.perform(multipart("/user/files/upload")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("文件大小不能超过 5MB"));
    }

    // ==================== 非法扩展名 ====================

    @Test
    public void testUploadInvalidExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                "fake content".getBytes()
        );

        mockMvc.perform(multipart("/user/files/upload")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("只允许上传 jpg/jpeg/png/gif/webp 格式的图片"));
    }
}