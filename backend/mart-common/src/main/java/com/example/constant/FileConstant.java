package com.example.constant;

/**
 * 文件模块常量
 */
public class FileConstant {

    // ==================== 存储类型 ====================
    public static final String STORAGE_ALIYUN_OSS = "aliyun-oss";
    public static final String STORAGE_LOCAL = "local";

    // ==================== 文件状态 ====================
    public static final String STATUS_UNUSED = "unused";
    public static final String STATUS_USED = "used";

    // ==================== 限制 ====================
    /** 单文件最大 5MB */
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /** 允许的图片扩展名 */
    public static final String[] ALLOWED_EXTENSIONS = {
            "jpg", "jpeg", "png", "gif", "webp"
    };
}