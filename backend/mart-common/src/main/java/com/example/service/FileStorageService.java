package com.example.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储接口
 *
 * 实现类：
 * - AliOssFileStorageService  阿里云 OSS
 * - LocalFileStorageService   本地磁盘
 *
 * 通过 campus.file.storage-type 切换
 */
public interface FileStorageService {

    /**
     * 上传文件，返回可访问的 URL
     *
     * @param file     文件
     * @param fileName 存储文件名（由上层生成，不含路径）
     * @return 可访问的 URL
     */
    String upload(MultipartFile file, String fileName);

    /**
     * 删除文件
     *
     * @param url 文件访问 URL
     */
    void delete(String url);

    /**
     * 获取存储类型标识
     */
    String getStorageType();
}