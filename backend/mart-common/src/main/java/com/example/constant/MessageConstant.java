package com.example.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐,不能删除";
    public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品,不能删除";
    public static final String SHOPPING_CART_IS_NULL = "购物车数据为空，不能下单";
    public static final String ADDRESS_BOOK_IS_NULL = "用户地址为空，不能下单";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String SETMEAL_ENABLE_FAILED = "套餐内包含未启售菜品，无法启售";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String SETMEAL_ON_SALE = "起售中的套餐不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐,不能删除";


    // ===== 认证模块新增 =====
    public static final String CODE_ERROR = "验证码错误或已过期";
    public static final String USERNAME_EXISTS = "用户名已存在";
    public static final String PHONE_EXISTS = "手机号已注册";
    public static final String PHONE_NOT_REGISTERED = "手机号未注册";
    public static final String ACCOUNT_BANNED = "账号已被封禁";
    public static final String USERNAME_OR_PASSWORD_ERROR = "用户名或密码错误";
    public static final String USER_NOT_FOUND = "用户不存在";


    // ===== 个人中心 =====
    public static final String OLD_PASSWORD_ERROR = "原密码错误";
    public static final String PASSWORD_SAME = "新密码不能与原密码相同";

    // ===== 校园认证 =====
    public static final String CERTIFICATION_PENDING = "已有待审核的认证，请勿重复提交";
    public static final String CERTIFICATION_APPROVED = "已通过校园认证，无需重复提交";
    public static final String CERTIFICATION_NOT_FOUND = "认证记录不存在";

    // ===== 地址 =====
    public static final String ADDRESS_NOT_FOUND = "地址不存在";
    public static final String ADDRESS_NO_PERMISSION = "无权操作该地址";

    // ===== 商品 =====
    public static final String PRODUCT_NOT_FOUND = "商品不存在";
    public static final String PRODUCT_OFFLINE = "商品已下架";
    public static final String PRODUCT_ALREADY_FAVORITED = "已收藏该商品";
    public static final String PRODUCT_NOT_FAVORITED = "未收藏该商品";

    // ===== 我的商店 =====
    public static final String PRODUCT_NOT_YOURS = "无权操作该商品";
    public static final String PRODUCT_SOLD_CANNOT_EDIT = "已售出商品不能修改";
    public static final String PRODUCT_RESERVED_CANNOT_DELETE = "商品已被预订，无法删除";
    public static final String PRODUCT_SOLD_CANNOT_DELETE = "已成交商品不能删除";
    public static final String PRODUCT_ALREADY_OFFLINE = "商品已是下架状态";
    public static final String PRODUCT_ALREADY_ON_SALE = "商品已是上架状态";
    public static final String PRODUCT_AUDIT_NOT_PASSED = "商品审核未通过，不能上架";
    public static final String CATEGORY_NOT_FOUND = "分类不存在";
    public static final String CATEGORY_DISABLED = "分类已禁用";

    // ===== 订单 =====
    public static final String ORDER_NOT_FOUND = "订单不存在";
    public static final String ORDER_NO_PERMISSION = "无权操作该订单";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String ORDER_PRODUCT_NOT_AVAILABLE = "商品已下架或已被预订";
    public static final String ORDER_CANNOT_BUY_OWN = "不能购买自己的商品";
    public static final String ORDER_ALREADY_EXISTS = "该商品已有待处理的订单";
    public static final String ORDER_BUYER_NOT_MATCH = "只有买家可以确认完成";
    public static final String ORDER_SELLER_NOT_MATCH = "只有卖家可以操作";

    // ===== 评价 =====
    public static final String REVIEW_ORDER_NOT_COMPLETED = "订单未完成，不能评价";
    public static final String REVIEW_ALREADY_EXISTS = "您已评价过该订单";
    public static final String REVIEW_NOT_FOUND = "评价不存在";
    public static final String REVIEW_NO_PERMISSION = "无权操作该评价";
    public static final String REVIEW_REPLY_ALREADY_EXISTS = "该评价已回复过";
    public static final String REVIEW_ONLY_RECEIVER_CAN_REPLY = "只有被评价人可以回复";

    // ===== 消息 =====
    public static final String MESSAGE_NOT_FOUND = "消息不存在";
    public static final String MESSAGE_NO_PERMISSION = "无权操作该消息";

    // ===== 举报 =====
    public static final String REPORT_NOT_FOUND = "举报不存在";
    public static final String REPORT_NO_PERMISSION = "无权操作该举报";
    public static final String REPORT_TARGET_NOT_FOUND = "举报对象不存在";
    public static final String REPORT_ALREADY_EXISTS = "您已举报过该对象，请勿重复提交";
    public static final String REPORT_CANNOT_REPORT_SELF = "不能举报自己";
    public static final String APPEAL_NOT_FOUND = "申诉不存在";
    public static final String APPEAL_ALREADY_EXISTS = "该举报已申诉过";
    public static final String APPEAL_ONLY_REPORTED_CAN = "只有被举报人可以申诉";
    public static final String APPEAL_REPORT_NOT_HANDLED = "举报尚未处理，无法申诉";

    // ===== 私信 =====
    public static final String CONVERSATION_NOT_FOUND = "会话不存在";
    public static final String CONVERSATION_NO_PERMISSION = "无权操作该会话";
    public static final String CHAT_CANNOT_SEND_TO_SELF = "不能给自己发消息";
    public static final String CHAT_TARGET_USER_NOT_FOUND = "对方用户不存在";

    // ==================== 管理端公告管理 ====================
    public static final String ANNOUNCEMENT_NOT_FOUND = "公告不存在";
    public static final String ANNOUNCEMENT_STATUS_INVALID = "公告状态不合法";

}
