package com.unilife.mq;

/**
 * MQ 主题 / 消费组 / 延迟等级统一定义
 */
public class MqConstants {

    /** 秒杀下单主题：抢购资格校验通过后异步落库 */
    public static final String TOPIC_COUPON_ORDER = "unilife-coupon-order-topic";
    /** 超时关单主题：延迟消息分等级检查未支付订单 */
    public static final String TOPIC_ORDER_CLOSE = "unilife-order-close-topic";
    /** 缓存删除补偿主题：删缓存失败后重试 */
    public static final String TOPIC_CACHE_RETRY = "unilife-cache-retry-topic";

    public static final String GROUP_ORDER_CREATE = "unilife-order-create-group";
    public static final String GROUP_ORDER_CLOSE = "unilife-order-close-group";
    public static final String GROUP_CACHE_RETRY = "unilife-cache-retry-group";

    /** 订单支付超时时间：15 分钟 */
    public static final long ORDER_TIMEOUT_SECONDS = 15 * 60L;

    /**
     * RocketMQ 内置 18 个延迟等级对应的秒数（等级 = 数组下标 + 1）：
     * 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    public static final long[] DELAY_LEVEL_SECONDS = {
            1, 5, 10, 30, 60, 120, 180, 240, 300,
            360, 420, 480, 540, 600, 1200, 1800, 3600, 7200
    };
}
