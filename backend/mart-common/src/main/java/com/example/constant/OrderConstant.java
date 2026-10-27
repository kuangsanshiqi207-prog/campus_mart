package com.example.constant;

public class OrderConstant {

    // ==================== 订单状态 ====================
    /** 待卖家确认 */
    public static final String STATUS_PENDING = "pending";
    /** 卖家已接受，待买家支付 */
    public static final String STATUS_ACCEPTED = "accepted";
    /** 买家已支付，待确认成交 */
    public static final String STATUS_PAID = "paid";
    /** 卖家已拒绝 */
    public static final String STATUS_REJECTED = "rejected";
    /** 已完成 */
    public static final String STATUS_COMPLETED = "completed";
    /** 已取消 */
    public static final String STATUS_CANCELLED = "cancelled";

    // ==================== 订单视角 ====================
    public static final String TYPE_BUY = "buy";
    public static final String TYPE_SELL = "sell";
}