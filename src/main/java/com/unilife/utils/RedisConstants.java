package com.unilife.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    /** 空值缓存的过期时间（分钟），用于防缓存穿透 */
    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_MERCHANT_TTL = 30L;
    public static final String CACHE_MERCHANT_KEY = "cache:merchant:";

    public static final String LOCK_MERCHANT_KEY = "lock:merchant:";
    public static final Long LOCK_MERCHANT_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:coupon:stock:";
    public static final String SECKILL_ORDER_KEY = "seckill:coupon:order:";
    public static final String LOCK_CREATE_ORDER_KEY = "lock:coupon:order:";
    public static final String RATE_LIMIT_KEY = "rate:limit:";

    public static final String POST_LIKED_KEY = "post:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String MERCHANT_GEO_KEY = "merchant:geo:";
    public static final String USER_SIGN_KEY = "sign:";

    public static final String MERCHANT_TYPE_KEY = "merchant_type:";
    public static final Long MERCHANT_TYPE_LONG = 10L;
}
