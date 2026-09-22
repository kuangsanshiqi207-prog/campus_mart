package com.example.constant;

/**
 * 用户模块常量
 */
public class UserConstant {

    // ==================== 角色 ====================
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // ==================== 账号状态 ====================
    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_BANNED = "banned";

    // ==================== 校园认证 ====================
    public static final Integer NOT_CERTIFIED = 0;
    public static final Integer CERTIFIED = 1;

    // ==================== 默认值 ====================
    public static final Integer DEFAULT_CREDIT_SCORE = 100;
    public static final String DEFAULT_NICKNAME_PREFIX = "用户";
    public static final Integer DEFAULT_NICKNAME_RANDOM_LENGTH = 6;

    // ==================== 认证状态 ====================
    public static final String CERT_STATUS_PENDING = "pending";
    public static final String CERT_STATUS_APPROVED = "approved";
    public static final String CERT_STATUS_REJECTED = "rejected";

    // ==================== 地址默认 ====================
    public static final Integer ADDRESS_NOT_DEFAULT = 0;
    public static final Integer ADDRESS_DEFAULT = 1;
}