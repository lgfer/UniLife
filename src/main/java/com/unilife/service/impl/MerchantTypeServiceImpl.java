package com.unilife.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unilife.dto.Result;
import com.unilife.entity.MerchantType;
import com.unilife.mapper.MerchantTypeMapper;
import com.unilife.service.IMerchantTypeService;
import com.unilife.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MerchantTypeServiceImpl extends ServiceImpl<MerchantTypeMapper, MerchantType> implements IMerchantTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 商家分类列表：典型的读多写少数据，整表缓存进 Redis List 并设置过期 */
    @Override
    public Result querySort() {
        List<String> typeJsonList = stringRedisTemplate.opsForList()
                .range(RedisConstants.MERCHANT_TYPE_KEY, 0, -1);
        if (typeJsonList != null && !typeJsonList.isEmpty()) {
            List<MerchantType> types = typeJsonList.stream()
                    .map(json -> JSONUtil.toBean(json, MerchantType.class))
                    .collect(Collectors.toList());
            return Result.ok(types);
        }
        List<MerchantType> types = query().orderByAsc("sort").list();
        if (types == null || types.isEmpty()) {
            return Result.fail("没有分类数据");
        }
        for (MerchantType type : types) {
            stringRedisTemplate.opsForList()
                    .rightPush(RedisConstants.MERCHANT_TYPE_KEY, JSONUtil.toJsonStr(type));
        }
        stringRedisTemplate.expire(RedisConstants.MERCHANT_TYPE_KEY,
                RedisConstants.MERCHANT_TYPE_LONG, TimeUnit.MINUTES);
        return Result.ok(types);
    }
}
