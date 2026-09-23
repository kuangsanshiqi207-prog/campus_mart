package com.example.constant;

/**
 * 商品模块常量
 */
public class ProductConstant {

    // ==================== 商品上架状态 ====================
    public static final String STATUS_ON_SALE = "on_sale";
    public static final String STATUS_RESERVED = "reserved";
    public static final String STATUS_SOLD = "sold";
    public static final String STATUS_OFFLINE = "offline";

    // ==================== 商品审核状态 ====================
    public static final String AUDIT_PENDING = "pending";
    public static final String AUDIT_APPROVED = "approved";
    public static final String AUDIT_REJECTED = "rejected";

    // ==================== 商品成色 ====================
    public static final String CONDITION_NEW = "new";
    public static final String CONDITION_ALMOST_NEW = "almost_new";
    public static final String CONDITION_GOOD = "good";
    public static final String CONDITION_NORMAL = "normal";

    // ==================== 交易方式 ====================
    public static final String TRADE_FACE = "face";
    public static final String TRADE_EXPRESS = "express";
    public static final String TRADE_SELF_PICKUP = "self_pickup";

    // ==================== 排序方式 ====================
    public static final String SORT_LATEST = "latest";
    public static final String SORT_PRICE_ASC = "price_asc";
    public static final String SORT_PRICE_DESC = "price_desc";
    public static final String SORT_HOT = "hot";

    // ==================== 分类状态 ====================
    public static final Integer CATEGORY_ENABLED = 1;
    public static final Integer CATEGORY_DISABLED = 0;
}