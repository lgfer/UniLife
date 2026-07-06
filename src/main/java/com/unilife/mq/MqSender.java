package com.unilife.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * MQ 发送统一入口
 */
@Slf4j
@Component
public class MqSender {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /** 发送秒杀下单消息（同步发送并校验结果，失败抛异常交给上游回滚 Redis 预扣） */
    public void sendOrderCreate(String orderJson) {
        SendResult result = rocketMQTemplate.syncSend(MqConstants.TOPIC_COUPON_ORDER, orderJson);
        if (result == null || result.getSendStatus() != SendStatus.SEND_OK) {
            throw new RuntimeException("下单消息发送失败：" + (result == null ? "null" : result.getSendStatus()));
        }
    }

    /**
     * 投递"检查并关闭超时订单"的延迟消息。
     *
     * RocketMQ 开源版只提供 18 个固定延迟等级，不支持任意时长延迟，
     * 因此采用分等级逼近：每次选一个不超过剩余时间的最大等级投递，
     * 消费端到期后重新计算剩余时间，未到超时点就用更小的等级续投，
     * 逐级逼近精确的超时时刻（如 15 分钟 = 10 分钟一级 + 5 分钟一级）。
     */
    public void sendOrderCloseDelay(Long orderId, long delaySeconds) {
        int level = pickDelayLevel(delaySeconds);
        rocketMQTemplate.syncSend(
                MqConstants.TOPIC_ORDER_CLOSE,
                MessageBuilder.withPayload(String.valueOf(orderId)).build(),
                3000,
                level
        );
        log.debug("已投递关单延迟消息：orderId={}，期望延迟={}s，实际等级={}（{}s）",
                orderId, delaySeconds, level, MqConstants.DELAY_LEVEL_SECONDS[level - 1]);
    }

    /** 删缓存失败后，把 key 投给补偿队列重试删除 */
    public void sendCacheRetry(String cacheKey) {
        rocketMQTemplate.syncSend(MqConstants.TOPIC_CACHE_RETRY, cacheKey);
    }

    /** 选择不超过期望延迟时间的最大延迟等级；小于最小等级时取等级 1（1 秒） */
    private int pickDelayLevel(long delaySeconds) {
        long[] levels = MqConstants.DELAY_LEVEL_SECONDS;
        for (int i = levels.length - 1; i >= 0; i--) {
            if (levels[i] <= delaySeconds) {
                return i + 1;
            }
        }
        return 1;
    }
}
