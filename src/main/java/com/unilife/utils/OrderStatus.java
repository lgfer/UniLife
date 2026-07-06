package com.unilife.utils;

/**
 * 订单状态：1 未支付；2 已支付；3 已核销；4 已取消（超时关单）；5 退款中；6 已退款
 */
public class OrderStatus {
    public static final Integer UNPAID = 1;
    public static final Integer PAID = 2;
    public static final Integer USED = 3;
    public static final Integer CLOSED = 4;
    public static final Integer REFUNDING = 5;
    public static final Integer REFUNDED = 6;
}
