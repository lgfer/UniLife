package com.unilife.ratelimit;

/**
 * 触发限流时抛出，由全局异常处理器统一转为友好提示
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
