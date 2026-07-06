package com.unilife.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unilife.dto.Result;
import com.unilife.entity.Coupon;
import com.unilife.mapper.CouponMapper;
import com.unilife.entity.SeckillCoupon;
import com.unilife.service.ISeckillCouponService;
import com.unilife.service.ICouponService;
import com.unilife.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.unilife.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    @Resource
    private ISeckillCouponService seckillCouponService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryCouponOfMerchant(Long merchantId) {
        // 查询优惠券信息
        List<Coupon> coupons = getBaseMapper().queryCouponOfMerchant(merchantId);
        // 返回结果
        return Result.ok(coupons);
    }

    @Override
    @Transactional
    public void addSeckillCoupon(Coupon coupon) {
        // 保存优惠券
        save(coupon);
        if (coupon.getStock() == null || coupon.getBeginTime() == null) {
            // 普通券没有秒杀信息，到此为止
            return;
        }
        // 保存秒杀信息
        SeckillCoupon seckillCoupon = new SeckillCoupon();
        seckillCoupon.setCouponId(coupon.getId());
        seckillCoupon.setStock(coupon.getStock());
        seckillCoupon.setBeginTime(coupon.getBeginTime());
        seckillCoupon.setEndTime(coupon.getEndTime());
        seckillCouponService.save(seckillCoupon);
        // 秒杀库存预热到 Redis，抢购时 Lua 直接在缓存里扣减
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY +coupon.getId(),coupon.getStock().toString());

    }
}
