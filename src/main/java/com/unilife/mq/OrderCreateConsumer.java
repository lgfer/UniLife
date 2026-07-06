package com.unilife.mq;

import cn.hutool.json.JSONUtil;
import com.unilife.entity.CouponOrder;
import com.unilife.service.ICouponOrderService;
import com.unilife.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 秒杀下单消费者。
 * 请求线程只做 Redis 资格校验就返回，真正的建单落库在这里异步完成，
 * 数据库写入压力被 MQ 削峰后平滑消化，核心接口的吞吐不再受 DB 限制。
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqConstants.TOPIC_COUPON_ORDER,
        consumerGroup = MqConstants.GROUP_ORDER_CREATE)
public class OrderCreateConsumer implements RocketMQListener<String> {

    @Resource
    private ICouponOrderService couponOrderService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private MqSender mqSender;

    @Override
    public void onMessage(String message) {
        CouponOrder order = JSONUtil.toBean(message, CouponOrder.class);
        Long userId = order.getUserId();

        // 分布式锁兜底：防止同一用户的消息被并发消费时穿透一人一单校验
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_CREATE_ORDER_KEY + userId);
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.error("同一用户的下单消息并发到达，跳过，userId={}", userId);
            return;
        }
        boolean created;
        try {
            created = couponOrderService.createCouponOrder(order);
        } finally {
            lock.unlock();
        }

        if (created) {
            // 建单成功，投递第一级延迟消息，到点检查是否超时未支付
            mqSender.sendOrderCloseDelay(order.getId(), MqConstants.ORDER_TIMEOUT_SECONDS);
        }
    }
}
