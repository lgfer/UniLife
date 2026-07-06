package com.unilife.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存工具：
 * queryWithPassThrough  空值缓存方案，防缓存穿透；
 * queryWithLogicalExpire 逻辑过期方案，防热点 Key 击穿（内置空值防穿透）。
 */
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    /** 缓存重建线程池：逻辑过期后的重建都在后台完成，不阻塞请求线程 */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /** 写入带逻辑过期时间的缓存：不设物理 TTL，把过期时间放在 value 里 */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 空值缓存防穿透：数据库也查不到时，缓存一个短 TTL 的空串，
     * 恶意请求不存在的 id 会命中空值直接返回，不会反复打到数据库。
     */
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        // 命中的是空值缓存
        if (json != null) {
            return null;
        }
        R r = dbFallback.apply(id);
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        this.set(key, r, time, unit);
        return r;
    }

    /**
     * 逻辑过期防击穿：
     * 命中且未过期 → 直接返回；
     * 命中但已过期 → 先返回旧值，抢到互斥锁的线程在后台异步重建，
     *               热点 Key"过期"瞬间的洪峰全部拿旧值走人，不会击穿到数据库；
     * 未命中 → 同步查库重建；库里也没有则写空值缓存，同样具备防穿透能力。
     */
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type,
                                            Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);

        // 1. 未命中：首次访问或未预热，同步查库重建
        if (json == null) {
            R r = dbFallback.apply(id);
            if (r == null) {
                // 空值缓存，防穿透
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            this.setWithLogicalExpire(key, r, time, unit);
            return r;
        }
        // 2. 命中空值缓存：该 id 不存在
        if (StrUtil.isBlank(json)) {
            return null;
        }
        // 3. 命中：检查逻辑过期时间
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
            return r;
        }
        // 4. 已过期：抢到互斥锁的线程后台异步重建，当前请求直接返回旧值
        String lockKey = "lock:" + key;
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R fresh = dbFallback.apply(id);
                    if (fresh == null) {
                        stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                    } else {
                        this.setWithLogicalExpire(key, fresh, time, unit);
                    }
                } catch (Exception e) {
                    log.error("缓存重建失败：{}", key, e);
                } finally {
                    unLock(lockKey);
                }
            });
        }
        return r;
    }

    /** 互斥锁：保证同一个 Key 同时只有一个线程在重建缓存 */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
