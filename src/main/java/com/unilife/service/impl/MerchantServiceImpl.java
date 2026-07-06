package com.unilife.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unilife.dto.Result;
import com.unilife.entity.Merchant;
import com.unilife.mapper.MerchantMapper;
import com.unilife.mq.MqSender;
import com.unilife.service.IMerchantService;
import com.unilife.utils.CacheClient;
import com.unilife.utils.RedisData;
import com.unilife.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.unilife.utils.RedisConstants.CACHE_MERCHANT_KEY;
import static com.unilife.utils.RedisConstants.CACHE_MERCHANT_TTL;
import static com.unilife.utils.RedisConstants.MERCHANT_GEO_KEY;

@Slf4j
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements IMerchantService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private MqSender mqSender;

    /**
     * 商家详情查询：
     * 热点 Key 用逻辑过期方案防击穿（过期后先返回旧值、后台异步重建，请求不阻塞）；
     * 查不到的 id 用空值缓存防穿透。两套防护的实现细节见 CacheClient。
     */
    @Override
    public Result queryById(Long id) {
        Merchant merchant = cacheClient.queryWithLogicalExpire(
                CACHE_MERCHANT_KEY, id, Merchant.class, this::getById,
                CACHE_MERCHANT_TTL, TimeUnit.MINUTES);
        if (merchant == null) {
            return Result.fail("商家不存在！");
        }
        return Result.ok(merchant);
    }

    /** 热点商家缓存预热：写入带逻辑过期时间的缓存（配合测试类批量预热） */
    public void saveMerchant2Redis(Long id, Long expireSeconds) {
        Merchant merchant = getById(id);
        RedisData redisData = new RedisData();
        redisData.setData(merchant);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(CACHE_MERCHANT_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 更新商家：先更新数据库，再删除缓存（删除比更新缓存更不容易产生脏写）。
     * 删除失败时投递 MQ 补偿重试；若补偿也全部失败，
     * 缓存自身的过期时间会在到期后触发重建，作为最终一致性的兜底。
     */
    @Override
    @Transactional
    public Result update(Merchant merchant) {
        Long id = merchant.getId();
        if (id == null) {
            return Result.fail("商家id不能为空");
        }
        // 1. 先更新数据库
        updateById(merchant);
        // 2. 再删除缓存，失败走 MQ 补偿
        String key = CACHE_MERCHANT_KEY + id;
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败，已投递补偿消息：{}", key, e);
            mqSender.sendCacheRetry(key);
        }
        return Result.ok();
    }

    @Override
    public Result queryMerchantByType(Integer typeId, Integer current, Double x, Double y) {
        // 1. 不带坐标：普通分页查询
        if (x == null || y == null) {
            Page<Merchant> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }
        // 2. 计算分页参数
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;

        // 3. 查询 Redis GEO：按距离排序取出 5000 米内的商家 id 与距离
        String key = MERCHANT_GEO_KEY + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .search(
                        key,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );
        if (results == null) {
            return Result.ok(Collections.emptyList());
        }
        // 4. 截取 from ~ end 实现分页
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            String merchantIdStr = result.getContent().getName();
            ids.add(Long.valueOf(merchantIdStr));
            distanceMap.put(merchantIdStr, result.getDistance());
        });
        // 5. 按 GEO 返回的顺序查库
        String idStr = StrUtil.join(",", ids);
        List<Merchant> merchants = query()
                .in("id", ids).last("order by field(id," + idStr + ")").list();
        for (Merchant merchant : merchants) {
            merchant.setDistance(distanceMap.get(merchant.getId().toString()).getValue());
        }
        return Result.ok(merchants);
    }
}
