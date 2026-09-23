package com.example.service.impl;

import com.example.constant.FileConstant;
import com.example.service.FileStorageService;
import com.example.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campus.file.storage-type", havingValue = "aliyun-oss")
public class AliOssFileStorageService implements FileStorageService {

    private final AliOssUtil aliOssUtil;

    @Override
    public String upload(MultipartFile file, String fileName) {
        try {
            byte[] bytes = file.getBytes();
            String url = aliOssUtil.upload(bytes, fileName);
            log.info("文件上传到 OSS 成功：{}", url);
            return url;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public void delete(String url) {
        // OSS 删除逻辑，MVP 阶段可以先不实现
        log.warn("OSS 删除暂未实现：{}", url);
    }

    @Override
    public String getStorageType() {
        return FileConstant.STORAGE_ALIYUN_OSS;
    }
}