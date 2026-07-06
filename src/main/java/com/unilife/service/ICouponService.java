package com.unilife.service;

import com.unilife.dto.Result;
import com.unilife.entity.Coupon;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface ICouponService extends IService<Coupon> {

    Result queryCouponOfMerchant(Long merchantId);

    void addSeckillCoupon(Coupon coupon);
}
