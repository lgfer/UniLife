package com.unilife.ratelimit;

/**
 * 限流维度
 */
public enum LimitDimension {
    /** 全局：所有请求共享一个窗口 */
    GLOBAL,
    /** 按客户端 IP 分别限流 */
    IP,
    /** 按登录用户分别限流，未登录时退化为按 IP */
    USER
}
