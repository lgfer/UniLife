package com.unilife.service.impl;

import com.unilife.entity.SeckillCoupon;
import com.unilife.mapper.SeckillCouponMapper;
import com.unilife.service.ISeckillCouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 */
@Service
public class SeckillCouponServiceImpl extends ServiceImpl<SeckillCouponMapper, SeckillCoupon> implements ISeckillCouponService {

}
