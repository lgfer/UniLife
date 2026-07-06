package com.unilife.controller;


import com.unilife.dto.Result;
import com.unilife.entity.Coupon;
import com.unilife.service.ICouponService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 */
@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Resource
    private ICouponService couponService;

    /**
     * 新增普通券
     * @param coupon 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    public Result addCoupon(@RequestBody Coupon coupon) {
        couponService.addSeckillCoupon(coupon);
        return Result.ok(coupon.getId());
    }

    /**
     * 新增秒杀券
     * @param coupon 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("seckill")
    public Result addSeckillCoupon(@RequestBody Coupon coupon) {
        couponService.addSeckillCoupon(coupon);
        return Result.ok(coupon.getId());
    }

    /**
     * 查询商家的优惠券列表
     * @param merchantId 商家id
     * @return 优惠券列表
     */
    @GetMapping("/list/{merchantId}")
    public Result queryCouponOfMerchant(@PathVariable("merchantId") Long merchantId) {
       return couponService.queryCouponOfMerchant(merchantId);
    }
}
