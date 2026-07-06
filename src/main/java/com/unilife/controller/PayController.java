package com.unilife.controller;

import com.unilife.dto.Result;
import com.unilife.service.ICouponOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 支付回调（模拟第三方支付平台的异步通知）
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Resource
    private ICouponOrderService couponOrderService;

    /** 模拟支付成功回调：真实场景由支付平台服务端调用并携带验签参数 */
    @PostMapping("/callback/{orderId}")
    public Result payCallback(@PathVariable("orderId") Long orderId) {
        return couponOrderService.handlePayCallback(orderId);
    }
}
