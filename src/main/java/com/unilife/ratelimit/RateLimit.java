package com.unilife.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 滑动窗口限流注解，可重复标注，实现同一接口的多维度叠加限流。
 * 例：单用户 1 次/秒 + 单 IP 20 次/秒 + 全局 2000 次/秒。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(RateLimits.class)
public @interface RateLimit {

    /** 限流维度 */
    LimitDimension dimension() default LimitDimension.GLOBAL;

    /** 窗口内允许的最大请求数 */
    int count();

    /** 窗口长度（秒） */
    int window();

    /** 业务 key，默认取"类名:方法名" */
    String key() default "";

    /** 被限流时的提示语 */
    String message() default "访问过于频繁，请稍后再试";
}
