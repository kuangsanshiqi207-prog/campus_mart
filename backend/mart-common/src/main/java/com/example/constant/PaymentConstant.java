package com.example.constant;

public class PaymentConstant {

    // ==================== 支付状态 ====================
    /** 待支付 */
    public static final String STATUS_PENDING = "pending";
    /** 已支付 */
    public static final String STATUS_PAID = "paid";
    /** 已关闭 */
    public static final String STATUS_CLOSED = "closed";
    /** 已退款 */
    public static final String STATUS_REFUNDED = "refunded";

    // ==================== 支付渠道 ====================
    /** 模拟支付 */
    public static final String CHANNEL_MOCK = "mock";
}
