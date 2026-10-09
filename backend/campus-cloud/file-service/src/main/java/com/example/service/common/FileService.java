package com.example.service.common;

import com.example.vo.common.FileVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件
     */
    FileVO upload(MultipartFile file);

    /**
     * 校验 fileId 归属并返回文件（供其他模块调用）
     */
    com.example.entity.File getFileOwnedByUser(Long fileId, Long userId);

    /**
     * 标记文件已使用
     */
    void markUsed(Long fileId);
}