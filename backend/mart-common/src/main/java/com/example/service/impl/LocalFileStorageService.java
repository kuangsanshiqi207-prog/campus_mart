package com.example.service.impl;

import com.example.constant.FileConstant;
import com.example.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@ConditionalOnProperty(name = "campus.file.storage-type", havingValue = "local")
public class LocalFileStorageService implements FileStorageService {

    /**
     * 本地存储根目录
     */
    @Value("${campus.file.local.base-path:./uploads}")
    private String basePath;

    /**
     * 本地访问域名（用于拼接返回 URL）
     */
    @Value("${campus.file.local.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Override
    public String upload(MultipartFile file, String fileName) {
        try {
            // 按日期分子目录
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String relativePath = datePath + "/" + fileName;

            Path fullPath = Paths.get(basePath, relativePath);
            Files.createDirectories(fullPath.getParent());
            file.transferTo(fullPath.toFile());

            String url = baseUrl + "/" + relativePath;
            log.info("文件保存到本地成功：{}", url);
            return url;
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new RuntimeException("文件保存失败：" + e.getMessage());
        }
    }

    @Override
    public void delete(String url) {
        // 本地删除逻辑，MVP 阶段可以先不实现
        log.warn("本地文件删除暂未实现：{}", url);
    }

    @Override
    public String getStorageType() {
        return FileConstant.STORAGE_LOCAL;
    }
}