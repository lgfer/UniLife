package com.unilife.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 缓存删除补偿消费者：更新数据库后删缓存失败时，消息进入这里重试删除。
 * 消费抛出异常时 RocketMQ 会按间隔递增自动重投（默认最多 16 次）；
 * 若重试仍全部失败，缓存自身的过期时间会作为最终兜底，保证最终一致。
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = MqConstants.TOPIC_CACHE_RETRY,
        consumerGroup = MqConstants.GROUP_CACHE_RETRY)
public class CacheRetryConsumer implements RocketMQListener<String> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(String cacheKey) {
        try {
            stringRedisTemplate.delete(cacheKey);
            log.info("缓存补偿删除成功：{}", cacheKey);
        } catch (Exception e) {
            log.error("缓存补偿删除失败，等待 MQ 重投：{}", cacheKey, e);
            // 抛出异常触发 RocketMQ 的自动重试
            throw e;
        }
    }
}
