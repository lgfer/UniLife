package com.unilife;

import com.unilife.entity.Merchant;
import com.unilife.service.impl.MerchantServiceImpl;
import com.unilife.utils.CacheClient;
import com.unilife.utils.RedisConstants;
import com.unilife.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.unilife.utils.RedisConstants.MERCHANT_GEO_KEY;

@SpringBootTest
class UniLifeApplicationTests {
    @Resource
    private MerchantServiceImpl merchantService;

    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private CacheClient client;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private ExecutorService es= Executors.newFixedThreadPool(500);


    @Test
    void testSaveMerchant() throws InterruptedException {
        Merchant merchant = merchantService.getById(1L);
        client.setWithLogicalExpire(RedisConstants.CACHE_MERCHANT_KEY+1L,merchant,30L, TimeUnit.MINUTES);

    }

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task=()->{
            for (int i = 0; i < 100; i++) {
                Long id = redisIdWorker.nextId("coupon_order");
                System.out.println("id = "+id);
            }
            latch.countDown();
        };
        long begin=System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();

        long end=System.currentTimeMillis();
        System.out.println("time："+(end-begin));
    }
    //导入redisgeo商家数据
    @Test
    void loadMerchantDate(){
        //1.查询商家信息
        List<Merchant> list = merchantService.list();

        //2.把商家分组，按照typeId分组，id一致的放到一个集合
        Map<Long, List<Merchant>> map = list.stream().collect(Collectors.groupingBy(Merchant::getTypeId));
        //3.分批完成写入redis
        for (Map.Entry<Long, List<Merchant>> entry : map.entrySet()) {
            //获取类型id
            Long typeId = entry.getKey();
            //获取同类型商家集合
            List<Merchant>  value = entry.getValue();

            String key=MERCHANT_GEO_KEY+typeId;
            List<RedisGeoCommands.GeoLocation<String>> locations=new ArrayList<>();
            for (Merchant merchant : value) {
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        merchant.getId().toString(),
                        new Point(merchant.getX(),merchant.getY())
                ));
            }
            //写入redis
            stringRedisTemplate.opsForGeo().add(key,locations);
        }
    }

}
