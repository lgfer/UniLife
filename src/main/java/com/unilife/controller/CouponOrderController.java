package com.unilife.controller;

import com.unilife.dto.Result;
import com.unilife.ratelimit.LimitDimension;
import com.unilife.ratelimit.RateLimit;
import com.unilife.service.ICouponOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/coupon-order")
public class CouponOrderController {

    @Resource
    private ICouponOrderService couponOrderService;

    /**
     * 秒杀下单。三级滑动窗口限流叠加：
     * 单用户 1 次/秒防连点与脚本抢券，单 IP 20 次/秒防单点刷量与爬虫，
     * 全局 2000 次/秒防系统过载，超阈值直接在切面被拦下，不进业务逻辑。
     */
    @RateLimit(dimension = LimitDimension.USER, count = 1, window = 1, message = "手速太快啦，请稍后再试")
    @RateLimit(dimension = LimitDimension.IP, count = 20, window = 1)
    @RateLimit(dimension = LimitDimension.GLOBAL, count = 2000, window = 1, message = "当前抢购人数过多，请稍后再试")
    @PostMapping("seckill/{id}")
    public Result seckillCoupon(@PathVariable("id") Long couponId) {
        return couponOrderService.seckillCoupon(couponId);
    }

    /** 查询订单详情（可用于演示订单状态流转：未支付 -> 已支付 / 已关闭） */
    @GetMapping("/{id}")
    public Result queryOrderById(@PathVariable("id") Long orderId) {
        return Result.ok(couponOrderService.getById(orderId));
    }
}
