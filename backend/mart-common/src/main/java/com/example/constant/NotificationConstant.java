package com.example.constant;

public class NotificationConstant {

    // ==================== 消息类型 ====================
    /** 交易相关 */
    public static final String TYPE_ORDER = "order";
    /** 审核相关 */
    public static final String TYPE_AUDIT = "audit";
    /** 举报相关 */
    public static final String TYPE_REPORT = "report";
    /** 系统通知 */
    public static final String TYPE_SYSTEM = "system";

    // ==================== 交易消息标题 ====================
    public static final String ORDER_CREATED_TITLE = "新的交易申请";
    public static final String ORDER_ACCEPTED_TITLE = "交易申请已接受";
    public static final String ORDER_REJECTED_TITLE = "交易申请被拒绝";
    public static final String ORDER_CANCELLED_TITLE = "订单已取消";
    public static final String ORDER_COMPLETED_TITLE = "交易已完成";
    public static final String ORDER_REVIEWED_TITLE = "收到新评价";

    // ==================== 交易消息内容模板 ====================
    public static final String ORDER_CREATED_CONTENT = "您发布的商品【%s】有用户申请交易，请及时处理。";
    public static final String ORDER_ACCEPTED_CONTENT = "卖家已接受您对【%s】的交易申请，请及时联系卖家约定面交。";
    public static final String ORDER_REJECTED_CONTENT = "卖家已拒绝您对【%s】的交易申请，原因：%s";
    public static final String ORDER_CANCELLED_CONTENT = "订单【%s】已取消，原因：%s";
    public static final String ORDER_COMPLETED_CONTENT = "订单【%s】已完成，感谢使用 CampusTrade。";
    public static final String ORDER_REVIEWED_CONTENT = "您的订单【%s】收到新评价，快去看看对方说了什么吧。";

    // ==================== 审核消息 ====================
    public static final String AUDIT_CERT_APPROVED_TITLE = "校园认证已通过";
    public static final String AUDIT_CERT_APPROVED_CONTENT = "恭喜，您的校园认证已通过，现在可以发布商品了。";
    public static final String AUDIT_CERT_REJECTED_TITLE = "校园认证被拒绝";
    public static final String AUDIT_CERT_REJECTED_CONTENT = "您的校园认证未通过，原因：%s";

    // ==================== 公告状态 ====================
    public static final String ANNOUNCEMENT_DRAFT = "draft";
    public static final String ANNOUNCEMENT_PUBLISHED = "published";
}