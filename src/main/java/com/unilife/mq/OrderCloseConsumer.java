package com.unilife.mq;

import com.unilife.service.ICouponOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 超时关单消费者：收到延迟消息后检查订单状态，
 * 未到超时点则续投更小等级的延迟消息，到点仍未支付则关单并回补库存。
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqConstants.TOPIC_ORDER_CLOSE,
        consumerGroup = MqConstants.GROUP_ORDER_CLOSE)
public class OrderCloseConsumer implements RocketMQListener<String> {

    @Resource
    private ICouponOrderService couponOrderService;

    @Override
    public void onMessage(String message) {
        couponOrderService.checkAndCloseOrder(Long.valueOf(message));
    }
}
