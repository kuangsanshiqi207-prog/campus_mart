package com.example.service.common.impl;

import cn.hutool.core.util.IdUtil;
import com.example.constant.FileConstant;
import com.example.context.BaseContext;
import com.example.entity.File;
import com.example.exception.BaseException;
import com.example.mapper.common.FileMapper;
import com.example.properties.FileProperties;
import com.example.service.FileStorageService;
import com.example.service.common.FileService;
import com.example.vo.common.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileStorageService fileStorageService;
    private final FileMapper fileMapper;
    private final FileProperties fileProperties;

    private static final String PATH_SEPARATOR = "/";

    @Override
    public FileVO upload(MultipartFile file) {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new BaseException("文件不能为空");
        }
        if (file.getSize() > FileConstant.MAX_FILE_SIZE) {
            throw new BaseException("文件大小不能超过 5MB");
        }

        // 2. 校验扩展名
        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        if (!isAllowedExtension(ext)) {
            throw new BaseException("只允许上传 jpg/jpeg/png/gif/webp 格式的图片");
        }

        // 3. 生成文件名：日期目录 + 雪花ID + 扩展名
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = datePath + PATH_SEPARATOR + IdUtil.getSnowflakeNextId() + "." + ext;

        // 4. 上传到存储
        String url = fileStorageService.upload(file, fileName);

        // 5. 写 file 表
        Long userId = BaseContext.getCurrentId();
        File fileEntity = File.builder()
                .id(IdUtil.getSnowflakeNextId())
                .userId(userId)
                .url(url)
                .originalName(originalName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .status(FileConstant.STATUS_UNUSED)
                .createTime(LocalDateTime.now())
                .build();
        fileMapper.insert(fileEntity);

        log.info("文件上传成功：userId={}, fileId={}, url={}", userId, fileEntity.getId(), url);

        return FileVO.builder()
                .fileId(fileEntity.getId())
                .url(url)
                .name(originalName)
                .size(file.getSize())
                .build();
    }

    @Override
    public File getFileOwnedByUser(Long fileId, Long userId) {
        if (fileId == null) {
            return null;
        }
        File file = fileMapper.getById(fileId);
        if (file == null) {
            throw new BaseException("文件不存在");
        }
        if (!file.getUserId().equals(userId)) {
            throw new BaseException("无权使用该文件");
        }
        return file;
    }

    @Override
    public void markUsed(Long fileId) {
        if (fileId != null) {
            fileMapper.markUsed(fileId);
        }
    }

    // ==================== 私有方法 ====================

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String ext) {
        return Arrays.stream(FileConstant.ALLOWED_EXTENSIONS)
                .anyMatch(allowed -> allowed.equalsIgnoreCase(ext));
    }
}