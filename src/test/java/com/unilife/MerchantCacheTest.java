package com.unilife;

import com.unilife.entity.Merchant;
import com.unilife.service.IMerchantService;
import com.unilife.utils.CacheClient;
import com.unilife.utils.RedisConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class MerchantCacheTest {

    @Autowired
    private IMerchantService merchantService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheClient cacheClient;

    @Test
    public void testCacheAllMerchants() {
        // 1. 从数据库中查询所有商家信息
        List<Merchant> merchantList = merchantService.list();

        // 2. 遍历商家列表，将每个商家的信息写入Redis
        for (Merchant merchant : merchantList) {
            // 生成Redis键
            String key = RedisConstants.CACHE_MERCHANT_KEY + merchant.getId();

            // 将商家信息写入Redis，并设置逻辑过期时间
            cacheClient.setWithLogicalExpire(key, merchant, RedisConstants.CACHE_MERCHANT_TTL, TimeUnit.HOURS);
        }

        System.out.println("All merchant information has been cached to Redis.");
    }
}
