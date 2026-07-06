package com.unilife.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unilife.dto.Result;
import com.unilife.entity.CouponOrder;

public interface ICouponOrderService extends IService<CouponOrder> {

    /** 秒杀下单入口：Lua 资格校验通过后发 MQ 异步落库 */
    Result seckillCoupon(Long couponId);

    /** MQ 消费侧：真正创建订单（幂等 + 一人一单兜底 + 条件扣库存） */
    boolean createCouponOrder(CouponOrder couponOrder);

    /** 模拟支付回调：乐观锁推进订单状态 未支付 -> 已支付 */
    Result handlePayCallback(Long orderId);

    /** 延迟消息消费侧：超时未支付则关单回补库存，未到点则续投 */
    void checkAndCloseOrder(Long orderId);
}
