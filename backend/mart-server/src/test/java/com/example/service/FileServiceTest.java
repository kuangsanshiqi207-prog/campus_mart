package com.example.service;

import com.example.context.BaseContext;
import com.example.entity.File;
import com.example.exception.BaseException;
import com.example.mapper.common.FileMapper;
import com.example.service.common.FileService;
import com.example.vo.common.FileVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileMapper fileMapper;

    /**
     * 用 MockitoBean 替换真实的 FileStorageService，
     * 避免测试时真的上传到 OSS
     */
    @MockitoBean
    private FileStorageService fileStorageService;

    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(TEST_USER_ID);
        when(fileStorageService.upload(any(), anyString()))
                .thenAnswer(invocation -> {
                    String fileName = invocation.getArgument(1);
                    return "https://mock.oss.com/" + fileName;
                });
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    // ==================== 正常上传 ====================

    @Test
    void testUploadSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        FileVO vo = fileService.upload(file);

        assertNotNull(vo, "上传后应返回 FileVO");
        assertNotNull(vo.getFileId(), "fileId 不应为 null");
        assertEquals("test.jpg", vo.getName(), "文件名应匹配");
        assertTrue(vo.getUrl().startsWith("https://mock.oss.com/"),
                "URL 应以 Mock 地址开头");

        File saved = fileMapper.getById(vo.getFileId());
        assertNotNull(saved, "文件应已入库");
        assertEquals(TEST_USER_ID, saved.getUserId(), "用户 ID 应匹配");
        assertEquals("test.jpg", saved.getOriginalName(), "原始文件名应匹配");
    }

    // ==================== 空文件 ====================

    @Test
    void testUploadEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        BaseException ex = assertThrows(BaseException.class,
                () -> fileService.upload(file),
                "上传空文件应抛出 BaseException");

        assertEquals("文件不能为空", ex.getMessage());
    }

    // ==================== 超过大小限制 ====================

    @Test
    void testUploadTooLarge() {
        byte[] bigContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "big.jpg",
                "image/jpeg",
                bigContent
        );

        BaseException ex = assertThrows(BaseException.class,
                () -> fileService.upload(file),
                "上传超大文件应抛出 BaseException");

        assertEquals("文件大小不能超过 5MB", ex.getMessage());
    }

    // ==================== 非法扩展名 ====================

    @Test
    void testUploadInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                "fake content".getBytes()
        );

        BaseException ex = assertThrows(BaseException.class,
                () -> fileService.upload(file),
                "上传非法扩展名应抛出 BaseException");

        assertTrue(ex.getMessage().contains("只允许上传"),
                "异常信息应包含扩展名限制说明");
    }

    // ==================== 校验归属 ====================

    @Test
    void testGetFileOwnedByUserSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );
        FileVO vo = fileService.upload(file);

        File got = fileService.getFileOwnedByUser(vo.getFileId(), TEST_USER_ID);

        assertNotNull(got, "应能获取到文件");
        assertEquals(vo.getFileId(), got.getId(), "文件 ID 应匹配");
        assertEquals(TEST_USER_ID, got.getUserId(), "归属用户应匹配");
    }

    @Test
    void testGetFileOwnedByUserWrongUser() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );
        FileVO vo = fileService.upload(file);

        Long fileId = vo.getFileId();

        BaseException ex = assertThrows(BaseException.class,
                () -> fileService.getFileOwnedByUser(fileId, 99999L),
                "访问他人文件应抛出 BaseException");


        assertEquals("无权使用该文件", ex.getMessage());
    }

    @Test
    void testGetFileOwnedByUserNotFound() {
        BaseException ex = assertThrows(BaseException.class,
                () -> fileService.getFileOwnedByUser(999999L, TEST_USER_ID),
                "查询不存在的文件应抛出 BaseException");

        assertEquals("文件不存在", ex.getMessage());
    }

    // ==================== 标记已使用 ====================

    @Test
    void testMarkUsed() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );
        FileVO vo = fileService.upload(file);

        fileService.markUsed(vo.getFileId());

        File updated = fileMapper.getById(vo.getFileId());
        assertNotNull(updated, "文件应存在");
        assertEquals("used", updated.getStatus(), "状态应更新为 used");
    }
}