package com.unilife.ratelimit;

import com.unilife.dto.UserDTO;
import com.unilife.utils.RedisConstants;
import com.unilife.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;

/**
 * 基于 Redis ZSET 的滑动窗口限流切面。
 *
 * 思路：每次请求以时间戳为 score 写入 ZSET，统计前先移除窗口外的旧记录，
 * 剩下的数量就是"最近 window 秒内的真实请求数"。窗口随时间平滑滑动，
 * 不存在固定窗口在临界点被打穿双倍流量的问题；
 * 判断与写入放在同一段 Lua 里原子执行，高并发下不会超量放行。
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> LIMIT_SCRIPT;

    static {
        LIMIT_SCRIPT = new DefaultRedisScript<>();
        LIMIT_SCRIPT.setLocation(new ClassPathResource("ratelimit.lua"));
        LIMIT_SCRIPT.setResultType(Long.class);
    }

    @Around("@annotation(com.unilife.ratelimit.RateLimit) || @annotation(com.unilife.ratelimit.RateLimits)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit[] rules = method.getAnnotationsByType(RateLimit.class);

        for (RateLimit rule : rules) {
            String bizKey = rule.key().isEmpty()
                    ? signature.getDeclaringType().getSimpleName() + ":" + method.getName()
                    : rule.key();
            String redisKey = RedisConstants.RATE_LIMIT_KEY + bizKey + ":" + buildDimensionKey(rule.dimension());
            long windowMillis = rule.window() * 1000L;
            // member 需要全局唯一，避免同一毫秒的两次请求在 ZSET 中相互覆盖
            String member = System.currentTimeMillis() + "-" + UUID.randomUUID();

            Long allowed = stringRedisTemplate.execute(
                    LIMIT_SCRIPT,
                    Collections.singletonList(redisKey),
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(windowMillis),
                    String.valueOf(rule.count()),
                    member
            );
            if (allowed == null || allowed == 0L) {
                log.warn("触发限流：key={}，规则={}次/{}秒", redisKey, rule.count(), rule.window());
                throw new RateLimitException(rule.message());
            }
        }
        return joinPoint.proceed();
    }

    /**
     * 组装维度 key：全局共用一个窗口 / 按 IP / 按用户（未登录退化为 IP）
     */
    private String buildDimensionKey(LimitDimension dimension) {
        switch (dimension) {
            case IP:
                return "ip:" + getClientIp();
            case USER:
                UserDTO user = UserHolder.getUser();
                return user != null ? "user:" + user.getId() : "ip:" + getClientIp();
            case GLOBAL:
            default:
                return "global";
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else if (ip.contains(",")) {
            // 多级代理时取第一段，即客户端真实 IP
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
